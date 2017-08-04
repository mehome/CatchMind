package com.catchmind.catchmind;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.IntDef;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
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

    public class ChatServiceBinder extends Binder {
        ChatService getService() {
            return ChatService.this; //현재 서비스를 반환.
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();


        mPref = getSharedPreferences("login",MODE_PRIVATE);
        editor = mPref.edit();
        userId = mPref.getString("userId","아이디없음");

        Log.d("serviceOncreate",userId);

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg){

                if(msg.what == 3) {
                    postConnect();
                }

            }
        };

        db = new MyDatabaseOpenHelper(this,"catchMind",null,1);

        ConnectThread ct = new ConnectThread();
        ct.start();


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

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
        public void recvData(String friendId,String content,String time); //액티비티에서 선언한 콜백 함수.
    }

    private ICallback mCallback;

    //액티비티에서 콜백 함수를 등록하기 위함.
    public void registerCallback(ICallback cb) {
        mCallback = cb;
    }

    //액티비티에서 서비스 함수를 호출하기 위한 함수 생성
    public void sendMessage(String friendId, String content, String time){
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

            Log.d("serviceConnectThread생성자",this.dstAddress+"##"+this.dstPort);
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

        public ReceiveThread(Socket threadsocket) {
            try{
                this.receiver = threadsocket.getInputStream();
                this.input = new DataInputStream(receiver);

            }catch (Exception e){

            }
        }


        @Override
        public void run() {

            while(true) {
                try {
                    while (receiver != null) {


                        try {
                            response = input.readUTF();
                        }catch (EOFException e){
                            e.printStackTrace();
                            Log.d("EOFE=Receive","힘들어");
                            return;
                        }

                        String friendId = null;
                        String content = null;
                        String time = null;

                        try {
                            JSONObject obj = new JSONObject(response);
                            friendId = obj.getString("friendId");
                            content = obj.getString("content");
                            time = obj.getString("time");

                        }catch(JSONException e){

                        }

                        db.insertMessageData(userId, friendId, content, time, 1);

                        if(boundCheck == true) {
                            mCallback.recvData(friendId, content, time);
                        }

                        response = "아이디는 " +friendId+"이고 내용은 "+content;
                        Log.d("핸들러","리시브안"+response);

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }




    public class SendThread extends Thread {

        String sendmsg;
        String friendId;
        String time;
        int no;

        boolean group_chat;

        OutputStream sender ;

        DataOutputStream output;

        public SendThread(Socket threadSocket,String friendId, String msg,String time) {

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

        public SendThread(Socket threadSocket,int no, String msg,String time) {

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
                    obj.put("content", this.sendmsg);
                    obj.put("time", time);
                    obj.put("groupchat",0);
                }

                this.sendmsg = obj.toString();

                output.writeUTF(sendmsg);

            }catch (IOException e){
                e.printStackTrace();
            }catch (JSONException e){
                e.printStackTrace();
            }


        }
    }



}
