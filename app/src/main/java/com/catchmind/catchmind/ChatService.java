package com.catchmind.catchmind;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.IntDef;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by sonsch94 on 2017-08-03.
 */

public class ChatService extends Service {

    Handler handler;
    Socket socket;
    public SharedPreferences mPref;
    public SharedPreferences.Editor editor;
    String userId;
    public MyDatabaseOpenHelper db;
    public boolean boundCheck;
    public boolean boundCheck_2;
    public ArrayList<String> chatFriendList = new ArrayList<>();


    public class ChatServiceBinder extends Binder {
        ChatService getService() {
            return ChatService.this; //현재 서비스를 반환.
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();




        handler = new Handler(){
            @Override
            public void handleMessage(Message msg){

                if(msg.what == 3) {
                    postConnect();
                }

            }
        };

//        db = new MyDatabaseOpenHelper(this,"catchMind",null,1);

//        ConnectThread ct = new ConnectThread();
//        ct.start();


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(socket != null) {
            try {
                socket.close();
            }catch(IOException e){
                e.printStackTrace();
            }
        }
        db = new MyDatabaseOpenHelper(this,"catchMind",null,1);

        mPref = getSharedPreferences("login",MODE_PRIVATE);
        editor = mPref.edit();
        userId = mPref.getString("userId","아이디없음");

        Log.d("ChatServiceOnStart",userId);

        ConnectThread ct = new ConnectThread();
        ct.start();

        chatFriendList = new ArrayList<String>();

        Cursor cursor = db.getChatFriendList(userId);
        while(cursor.moveToNext()) {

            chatFriendList.add(cursor.getString(1));
            Log.d("커서야ChatServiceOnStart",cursor.getString(0)+"#####"+cursor.getString(1)+"#####"+cursor.getString(2)+"#####"+cursor.getString(3));

        }
        return super.onStartCommand(intent, flags, startId);
    }

    private final IBinder mBinder = new ChatServiceBinder();

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return mBinder;
    }

    //콜백 인터페이스 선언
    public interface ICallback {
        public void recvData(String friendId,String content,long time); //액티비티에서 선언한 콜백 함수.
    }

    public interface ICallback_2{
        public void recvData(); //액티비티에서 선언한 콜백 함수.
        public void changeRoomList();
    }

    private ICallback mCallback;
    private ICallback_2 mCallback_2;

    //액티비티에서 콜백 함수를 등록하기 위함.
    public void registerCallback(ICallback cb) {
        mCallback = cb;
    }

    public void registerCallback_2(ICallback_2 cb_2) {
        mCallback_2 = cb_2;
    }

    //액티비티에서 서비스 함수를 호출하기 위한 함수 생성
    public void sendMessage(String friendId, String content, long time){
        SendThread st = new SendThread(socket,friendId, content, time);
        st.start();
    }

    public void postConnect(){
        ReceiveThread startReceive = new ReceiveThread(socket);
        startReceive.start();
    }

    public class ConnectThread extends Thread {

        String dstAddress;
        int dstPort;

        public ConnectThread(){
            this.dstAddress = "115.71.233.144";
            this.dstPort = 5000;

            Log.d("serviceConnectThread생성자",this.dstAddress+"##"+this.dstPort+"##"+userId);
        }

        @Override
        public void run() {

            try {

                socket = new Socket(dstAddress, 5000);

            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            try {

                OutputStream sender = socket.getOutputStream();
                DataOutputStream output = new DataOutputStream(sender);
                String sendData = userId;
                output.writeUTF(sendData);

                Message message= Message.obtain();
                message.what = 3;
                handler.sendMessage(message);

            }catch (IOException e){
                e.printStackTrace();
            }

        }
    }


    public class ReceiveThread extends Thread {

        String response;
        InputStream receiver;
        DataInputStream input;
        String checkId;

        public ReceiveThread(Socket threadsocket) {
            this.checkId = userId;
            try{
                this.receiver = threadsocket.getInputStream();
                this.input = new DataInputStream(receiver);

            }catch (Exception e){

            }
        }


        @Override
        public void run() {

            while(true) {
                if(!userId.equals(checkId)){
                    if(socket != null) {
                        try {
                            socket.close();
                        }catch(IOException e){
                            e.printStackTrace();
                        }
                    }
                    break;
                }
                try {
//                    while (receiver != null) {
                        Log.d("리시브살아있나",checkId+"####"+socket.toString());

                        try {
                            response = input.readUTF();
                        }catch (EOFException e){
                            e.printStackTrace();
                            Log.d("EOFE=Receive","힘들어");
                            return;
                        }

                        String friendId = null;
                        String content = null;
                        long time = 0 ;

                        try {
                            JSONObject obj = new JSONObject(response);
                            friendId = obj.getString("friendId");
                            content = obj.getString("content");
                            time = obj.getLong("time");

                        }catch(JSONException e){

                        }
                        Log.d("getFriendBoolean",chatFriendList.contains(friendId)+"");
                        if(!chatFriendList.contains(friendId)){
                            getFriendThread gft = new getFriendThread(friendId,time);
                            Log.d("getFriend이프안",chatFriendList.contains(friendId)+"");
                            gft.start();

                            try {

                                gft.join();

                            }catch(InterruptedException e){
                                e.printStackTrace();
                            }

                            chatFriendList.add(friendId);
                            Log.d("getFriend이프끝",chatFriendList.contains(friendId)+"");

                            if(boundCheck_2 == true){
                                mCallback_2.changeRoomList();
                            }

                        }

                        db.insertMessageData(userId, friendId, content, time, 1);

                        if(boundCheck == true) {
                            mCallback.recvData(friendId, content, time);
                        }

                        if(boundCheck_2 == true) {
                            mCallback_2.recvData();
                        }

                        response = "아이디는 " +friendId+"이고 내용은 "+content;
                        Log.d("핸들러","리시브안"+response);

//                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }




    public class SendThread extends Thread {

        String sendmsg;
        String friendId;
        long time;
        int no;

        boolean group_chat;

        OutputStream sender ;

        DataOutputStream output;

        public SendThread(Socket threadSocket,String friendId, String msg,long time) {

            this.friendId = friendId;
            this.sendmsg = msg;
            this.time = time;
            this.group_chat = false;

            try {
                this.sender = threadSocket.getOutputStream();
                this.output = new DataOutputStream(sender);
            }catch (IOException e){
                e.printStackTrace();
            }

            Log.d("내용물1t",sendmsg);

        }

        public SendThread(Socket threadSocket,int no, String msg,long time) {

            this.no = no;
            this.sendmsg = msg;
            this.time = time;
            this.group_chat = true;

            try {
                this.sender = threadSocket.getOutputStream();
                this.output = new DataOutputStream(sender);
            }catch (IOException e){
                e.printStackTrace();
            }

            Log.d("내용물1t",sendmsg);

        }

        @Override
        public void run() {
            Log.d("getFriendId",friendId);
            if(!chatFriendList.contains(friendId)){
                getFriendThread gft = new getFriendThread(friendId,time);
                Log.d("getFriend이프안2",chatFriendList.contains(friendId)+"");
                gft.start();

                try {

                    gft.join();

                }catch(InterruptedException e){
                    e.printStackTrace();
                }

                chatFriendList.add(friendId);
                Log.d("getFriend이프끝2",chatFriendList.contains(friendId)+"");

                if(boundCheck_2 == true){
                    mCallback_2.changeRoomList();
                }
            }

            try {

                JSONObject obj = new JSONObject();

                if(this.group_chat){
                    obj.put("userId", userId);
                    obj.put("no", this.no);
                    obj.put("content", this.sendmsg);
                    obj.put("time", time);
                    obj.put("groupchat",1);
                }else {
                    obj.put("userId", userId);
                    obj.put("friendId", friendId);
                    Log.d("최종",userId);
                    Log.d("최종",friendId);
                    obj.put("content", this.sendmsg);
                    obj.put("time", time);
                    obj.put("groupchat",0);
                }

                this.sendmsg = obj.toString();

                output.writeUTF(sendmsg);

                if(boundCheck_2 == true) {
                    mCallback_2.recvData();
                }

            }catch (IOException e){
                e.printStackTrace();
            }catch (JSONException e){
                e.printStackTrace();
            }


        }
    }

    public class getFriendThread extends Thread{

        public String sFriendId;
        public long sTime;

        public getFriendThread(String friendId,long time){
            this.sFriendId = friendId;
            this.sTime = time;
            Log.d("getFriend",sFriendId+time);
        }

        @Override
        public void run() {
            String data="";

            /* 인풋 파라메터값 생성 */
            String param = "friendId=" + this.sFriendId;
            try {
            /* 서버연결 */
                URL url = new URL("http://vnschat.vps.phps.kr/getFriend.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.connect();

            /* 안드로이드 -> 서버 파라메터값 전달 */
                OutputStream outs = conn.getOutputStream();
                outs.write(param.getBytes("UTF-8"));
                outs.flush();
                outs.close();



            /* 서버 -> 안드로이드 파라메터값 전달 */
                InputStream is = null;
                BufferedReader in = null;

                is = conn.getInputStream();
                in = new BufferedReader(new InputStreamReader(is), 8 * 1024);
                String line = null;
                StringBuffer buff = new StringBuffer();
                while ( ( line = in.readLine() ) != null )
                {
                    buff.append(line + "\n");
                }
                data = buff.toString().trim();
                Log.e("getFriend",data);
                try {
                    JSONObject jobj = new JSONObject(data);
                    String friendId = jobj.getString("friendId");
                    String nickname = jobj.getString("nickname");
                    String message = jobj.getString("message");
                    String profile = jobj.getString("profile");

                    db.insertChatFriendData(userId,0,sFriendId,nickname,profile,message,sTime);
                    Log.d("db.ICFD",userId+"###"+sFriendId);
                    db.insertChatRoomData(userId,0,sFriendId);
                    Log.d("db.ICRD",userId+"###"+sFriendId);

                }catch (JSONException e){
                    e.printStackTrace();
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("서비스종료",userId);


    }
}
