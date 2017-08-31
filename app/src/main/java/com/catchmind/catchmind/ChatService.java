package com.catchmind.catchmind;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.IntDef;
import android.support.v4.app.NotificationCompat;
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
    public boolean boundStart;
    public ArrayList<String> chatRoomList = new ArrayList<>();
    public int boundedNo;
    public String boundedFriendId;


    public class ChatServiceBinder extends Binder {
        ChatService getService() {
            return ChatService.this; //현재 서비스를 반환.
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("ChatServiceOnCreate","크리에이트");



        handler = new Handler(){
            @Override
            public void handleMessage(Message msg){

                if(msg.what == 3) {
                    postConnect();
                }else if(msg.what == 4){
                    ConnectThread ct = new ConnectThread();
                    ct.start();
                    Log.d("잃고난뒤4로 재연결","4로재연결");
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
        Log.d("담배Net","ChatService mobile##");

//        if(intent != null){
//            if(intent.getBooleanExtra("MobileChange",false)){
//                socket = null;
//                Log.d("zzzz","어이없다");
//            }
//        }

        if(socket != null) {
            try {
                socket.close();
            }catch (IOException e){
                e.printStackTrace();
            }
            socket = null;
        }
        ConnectThread ct = new ConnectThread();
        ct.start();
        chatRoomList = new ArrayList<String>();

        Cursor cursor = db.getChatRoomList();
        while(cursor.moveToNext()) {

            if(cursor.getInt(0)==0) {
                chatRoomList.add(cursor.getString(1));
            }else{
                chatRoomList.add(cursor.getInt(0)+"");
            }

            Log.d("커서야ChatServiceOnStart",cursor.getString(0)+"#####"+cursor.getString(1)+"#####"+cursor.getString(2));
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
        public void changeNo(int no);
        public void sendMessageMark(String content,long time);
        public void sendInviteMark(String content,long time);
        public void resetHash();
        public void recvUpdate();
        public String getFriendId();
        public void resetToolbar();
        public void receivePath(String PATH);
        public void receiveClear();

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

    //액티비티에서 읽음 전송


    public void sendClear(int no,String friendId, String content, long time){
        if (no < 0){
            return;
        }

        SendThread st = new SendThread(socket, no, friendId, content, time , 11);
        st.start();

    }

    public void sendPATH(int no,String friendId, String content, long time){
        if (no < 0){
            return;
        }

        SendThread st = new SendThread(socket, no, friendId, content, time , 10);
        st.start();

    }


    public void sendRead(int no, String friendId, long time){

        if (no < 0){
            return;
        }

        SendThread st = new SendThread(socket, no, friendId, "justUpdate", time , 2);
        st.start();

    }


    public void sendInvite(int no, String friendId, String content, long time, String inviteId ){

        if(no < 0){
            return;
        }

        db.insertChatFriendDataMultipleByJoin(content, no);

        JSONObject jobject = new JSONObject();
        try {
            jobject.put("content", content);
            jobject.put("inviteId", inviteId);
        }catch (JSONException e){
            e.printStackTrace();
        }

        SendThread st = new SendThread(socket, no, friendId, jobject.toString(), time, 3);
        st.start();

        try {


            JSONArray jarray = new JSONArray(content);


            db.insertMessageData(userId, no, userId, content, time, 3);
            mCallback.sendInviteMark(content,time);


        }catch(JSONException e){
            e.printStackTrace();
        }

    }


    //액티비티에서 메세지 전송
    public void sendMessage(int no,String friendId, String content, long time){

        if(no < 0 ){
            getNoThread gnt = new getNoThread(no,friendId,time);
            gnt.start();
            try {
                gnt.join();
                no = gnt.returnNO();
                if(no >0){
                    if(boundCheck) {
                        mCallback.changeNo(no);
                        boundedNo = no;
                    }
                    Log.d("CS.sendMessage","액티비티도 no고쳐라: "+no);
                }
                if(!chatRoomList.contains(no+"")) {
                    db.insertChatFriendDataMultipleByJoin(friendId, no);
                    db.insertChatRoomData(no, "group", time);
                    chatRoomList.add(no + "");
                    if (boundCheck) {
                        mCallback.resetHash();
                        mCallback.recvUpdate();
                        mCallback.resetToolbar();
                        mCallback.changeNo(no);
                    }

                    if(boundCheck_2 == true){
                        mCallback_2.changeRoomList();
                    }

                }
            }catch (InterruptedException e){
                e.printStackTrace();
                Log.d("gnt.InterruptedExep","no: "+no);
            }

        }

        if( no == 0 && !chatRoomList.contains(friendId) ){


            Cursor cursor = db.getFriendData(friendId);
            cursor.moveToNext();

            String nickname = cursor.getString(1);
            String profile = cursor.getString(2);
            String message = cursor.getString(3);

            db.insertChatFriendData(0,friendId,nickname,profile,message,0);
            Log.d("db.ICFD",userId+"###"+friendId);
            db.insertChatRoomData(0,friendId,time);
            Log.d("db.ICRD",userId+"###"+friendId);

            addChatFriendThread acft = new addChatFriendThread(0,friendId,0);
            acft.start();


        }


            SendThread st = new SendThread(socket, no, friendId, content, time , 1);
            st.start();

            try {
                st.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


        if(st.isSuccess()){
            Log.d("st.isSucess",content+"####"+time);
            if(no ==0) {
                db.insertMessageData(userId, no, friendId, content, time, 2);
            }else{
                db.insertMessageData(userId, no, userId, content, time, 2);
            }

            mCallback.sendMessageMark(content,time);

        }else{
            if(no ==0) {
                db.insertMessageData(userId, no, friendId, content, time, 44);
            }else{
                db.insertMessageData(userId, no, userId, content, time, 44);
            }
        }

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
                Log.d("여긴지낫니1","보고싶다");
                if(socket != null) {
                    Log.d("여긴지낫니2","보고싶다");
                    socket.close();
                    socket = null;
                }
                socket = new Socket(dstAddress, 5000);
            } catch (UnknownHostException e) {
                Log.d("UnknwonHostE","여기?");
                e.printStackTrace();
            } catch (IOException e) {
                Log.d("IOE","여기?");

//                Intent serviceIntent = new Intent(getApplicationContext(),ChatService.class);
//                startService(serviceIntent);


                ConnectThread ct = new ConnectThread();
                ct.start();

                e.printStackTrace();
                return;
            }


            try {

                OutputStream sender = socket.getOutputStream();
                DataOutputStream output = new DataOutputStream(sender);
                String sendData = userId;
                output.writeUTF(sendData);

                Message message= Message.obtain();
                message.what = 3;
                handler.sendMessage(message);
                Log.d("생성자","끝마침"+sendData);

            }catch (IOException e){
                e.printStackTrace();
                Log.d("생성자","IOE예외");
            }catch (NullPointerException e){
                e.printStackTrace();
                Log.d("생성자","Null에외");
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
                    Log.d("checkId바껴서","checkId: "+checkId+", userId: "+userId);
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

                        int no = 0;
                        String friendId = null;
                        String content = null;
                        long time = 0 ;
                        int kind = 0;

                        try {
                            JSONObject obj = new JSONObject(response);
                            no = obj.getInt("no");
                            friendId = obj.getString("friendId");
                            content = obj.getString("content");
                            time = obj.getLong("time");
                            kind = obj.getInt("kind");

                            ReceiveMessageThread rmt = new ReceiveMessageThread(no,friendId,content,time,kind);
                            rmt.start();

                            Log.d("리시브데이터","no: "+no+", friendId: "+friendId+", content: "+content+", time: "+time+", kind: "+kind);

                        }catch(JSONException e){

                        }


//                    }

                } catch (IOException e) {
                    Log.d("리시브소켓exception?","힘들어IOE로");
                    justReconnect();
                    e.printStackTrace();




                    break;
                } catch (NullPointerException e){
                    Log.d("리시브소켓exception?","힘들어Null로");
                    loseReceive();
                    e.printStackTrace();
                    break;
                }

                Log.d("리시브 죽었당",userId +"####"+response);
//                try {
//                    tmp_socket.close();
//                }catch (IOException e){
//                    e.printStackTrace();
//                }
            }

        }
    }


    public void loseReceive(){

        if(!socket.isClosed()) {
            Log.d("loseReceive해제","여긴지나나");
            SendThread st = new SendThread(socket,0,"해제자","해체",0,33 );
            st.start();
            try {
                st.join();
            }catch (InterruptedException e){
                e.printStackTrace();
            }

        }

        try {
            socket.close();
            socket = null ;
        }catch (NullPointerException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }

        Message message= Message.obtain();
        message.what = 4;
        handler.sendMessage(message);
    }

    public void justReconnect(){
        try {
            socket.close();
            socket = null ;
        }catch (NullPointerException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }

        Message message= Message.obtain();
        message.what = 4;
        handler.sendMessage(message);
    }

    public class SendThread extends Thread {

        String sendmsg;
        String friendId;
        long time;
        int no;
        boolean success;
        int kind;

        OutputStream sender ;

        DataOutputStream output;

        public SendThread(Socket threadSocket,int no, String friendId, String msg,long time,int kind) {

            this.no = no;
            this.friendId = friendId;
            this.sendmsg = msg;
            this.time = time;
            this.success = true;
            this.kind = kind;


            try {
                this.sender = threadSocket.getOutputStream();
                this.output = new DataOutputStream(sender);
            }catch (IOException e){
                e.printStackTrace();
            }

            Log.d("SendThread.Socket: ",threadSocket.toString());
            Log.d("내용물sendThread",sendmsg);

        }



        @Override
        public void run() {
            Log.d("sendThreadId",friendId);


            try {

                JSONObject obj = new JSONObject();

                    obj.put("userId", userId);
                    obj.put("no", this.no);
                    obj.put("friendId", friendId);
                    Log.d("최종",userId);
                    Log.d("최종",friendId);
                    obj.put("content", this.sendmsg);
                    obj.put("time", time);
                    obj.put("kind", kind);

                    Log.d("SendThread여긴",friendId+"###"+sendmsg);

                this.sendmsg = obj.toString();

                output.writeUTF(sendmsg);

                Log.d("SendThread.Output: ",output.toString());

                if(boundCheck_2 == true) {
                    mCallback_2.recvData();
                }

            }catch (IOException e){
                Log.d("getFriendId_IOException",friendId+"###"+sendmsg);
                e.printStackTrace();
            }catch (JSONException e){
                e.printStackTrace();
            }catch (NullPointerException e){
                Log.d("wifi_Null","lte변경좆같네");
                e.printStackTrace();
            }


        }

        public boolean isSuccess(){
            return this.success;
        }


    }

    public class getFriendThread extends Thread{


        public String sFriendId;
        public String sContent;
        public long sTime;

//        public long sTime;

        public getFriendThread(String friendId,String content,long time){

            this.sFriendId = friendId;
            this.sContent = content;
            this.sTime = time;
            Log.d("getFriend",sFriendId);
        }

        @Override
        public void run() {
            String data="";

            /* 인풋 파라메터값 생성 */
            String param = "friendId=" + this.sFriendId +"&userId=" + userId + "&time=" + sTime;
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

                    db.insertChatFriendData(0,sFriendId,nickname,profile,message,sTime);
                    Log.d("db.ICFD",userId+"###"+sFriendId);
                    db.insertChatRoomData(0,sFriendId,0);
                    Log.d("db.ICRD",userId+"###"+sFriendId);
                    chatRoomList.add(sFriendId);

                    if(boundCheck_2 == true){
                        mCallback_2.changeRoomList();
                    }

//                    db.insertMessageData(userId,0,friendId, sContent, sTime, 1);

                    if(boundCheck == true) {
                        if(boundedNo == 0 && boundedFriendId.equals(sFriendId)) {
                            mCallback.recvData(sFriendId, sContent, sTime);
                        }
                    }

                    if(boundCheck_2 == true) {
                        mCallback_2.recvData();
                    }


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

    public void terminateService(){
        try {
            this.socket.close();
        }catch (IOException e){

            Log.d("chatService","terminateService-exception");
        }
        Log.d("chatService","terminateService");
        this.stopSelf();
    }


    public class getNoThread extends Thread{

        public String sFriendId;
        public int sNo;
        public long sTime;

        public getNoThread(int no,String friendId,long time){

            this.sNo = no;
            this.sFriendId = friendId;
            this.sTime = time;

            Log.d("getNoThread","no: "+no+",friendId: "+sFriendId +",time: "+sTime);
        }

        @Override
        public void run() {

            String data="";

            /* 인풋 파라메터값 생성 */
            String param = "userId="+userId+"&friendId="+this.sFriendId+"&time="+this.sTime;

            try {
            /* 서버연결 */
                URL url = new URL("http://vnschat.vps.phps.kr/getNo.php");
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
                Log.e("getNoThread.data",data);
                this.sNo = Integer.parseInt(data);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        public int returnNO(){
            return this.sNo;
        }


    }



    public class addChatFriendThread extends Thread{


        public String sFriendId;
        public int sNo;
        public long sTime;


        public addChatFriendThread(int no,String friendId,long time){

            this.sFriendId = friendId;
            this.sNo = no;
            this.sTime = time;

            Log.d("acft","no: "+no+",friendId: "+sFriendId);
        }

        @Override
        public void run() {

            String data="";

            /* 인풋 파라메터값 생성 */
            String param = "userId="+userId+"&friendId="+this.sFriendId+"&no="+this.sNo+"&time="+this.sTime;

            try {
            /* 서버연결 */
                URL url = new URL("http://vnschat.vps.phps.kr/addChatFriend.php");
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
                Log.e("acft.data",data);
                if(data.equals("ok")) {
                    chatRoomList.add(this.sFriendId);
                    if(boundCheck_2 == true){
                        mCallback_2.changeRoomList();
                    }
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }


    }




    public class getGroupThread extends Thread{

        public int sNo;
        public String sFriendId;
        public String sContent;
        public long sTime;

        public getGroupThread(int no,String friendId,String content,long time){

            this.sNo = no;
            this.sFriendId = friendId;
            this.sContent = content;
            this.sTime = time;
            Log.d("getGroupThread","Constructor 안,no: "+sNo);

        }

        @Override
        public void run() {

            String data="";

            /* 인풋 파라메터값 생성 */
            String param = "userId="+userId+"&no="+this.sNo;

            try {
            /* 서버연결 */
                URL url = new URL("http://vnschat.vps.phps.kr/getGroup.php");
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
                Log.e("getGroupThread.data",data);
                JSONArray chatArray = new JSONArray(data);
                db.insertChatFriendDataMultiple(data);
                db.insertChatRoomData(sNo,"group",0);
                chatRoomList.add(this.sNo+"");
                if(boundCheck_2 == true){
                    mCallback_2.changeRoomList();
                }

//                db.insertMessageData(userId,sNo,sFriendId,sContent, sTime, 1);

                if(boundCheck == true) {
                    if(boundedNo == sNo ) {
                        mCallback.recvData(sFriendId, sContent, sTime);
                    }
                }

                if(boundCheck_2 == true) {
                    mCallback_2.recvData();
                }


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e){
                e.printStackTrace();
                Log.d("getGroup","JSONException");
            }
        }


        public int returnNO(){
            return this.sNo;
        }


    }



//    public class ReceiveMessageThread extends Thread{
//
//        public int sNo;
//        public String sFriendId;
//        public String sContent;
//        public long sTime;
//
//        public ReceiveMessageThread(int no,String friendId,String content,long time){
//
//            this.sNo = no;
//            this.sFriendId = friendId;
//            this.sContent = content;
//            this.sTime = time;
//            Log.d("ReceiveMessageThread","Constructor 안,no: "+sNo +", sFriend: "+sFriendId + ", content: "+sContent);
//
//        }
//
//        @Override
//        public void run() {
//            db.insertMessageData(userId,sNo,sFriendId, sContent, sTime, 1);
//            if(boundStart) {
//                if(sNo == 0 ) {
//                    if(boundedNo == 0 && boundedFriendId == sFriendId) {
//                        db.updateChatRoomData(sNo, sFriendId, sTime);
//                        sendRead(sNo,mCallback.getFriendId(),sTime);
//                    }
//                }else{
//                    if(boundedNo == sNo) {
//                        db.updateChatRoomData(sNo, sFriendId, sTime);
//                        sendRead(sNo,mCallback.getFriendId(),sTime);
//                    }
//                }
//            }
//        }
//
//
//    }


//    public class ReceiveUpdateThread extends Thread{
//
//        public int sNo;
//        public String sFriendId;
//        public String sContent;
//        public long sTime;
//
//        public ReceiveUpdateThread(int no,String friendId,long time){
//
//            this.sNo = no;
//            this.sFriendId = friendId;
//            this.sTime = time;
//            Log.d("ReceiveUpdateThread","Constructor 안,no: "+sNo +", sFriend: "+sFriendId);
//
//        }
//
//        @Override
//        public void run() {
//
//            db.updateChatFriendData(sNo,sFriendId,sTime);
//
//            if(boundStart) {
//
//                if(sNo == 0 ) {
//                    if(boundedNo == 0 && boundedFriendId == sFriendId) {
//                        mCallback.recvUpdate();
//                    }
//                }else{
//                    if(boundedNo == sNo) {
//                        mCallback.recvUpdate();
//                    }
//                }
//
//            }
//
//        }
//
//
//    }





    public class ReceiveMessageThread extends Thread{

        public int sNo;
        public String sFriendId;
        public String sContent;
        public long sTime;
        public int sKind;

        public ReceiveMessageThread(int no,String friendId,String content,long time,int kind){

            this.sNo = no;
            this.sFriendId = friendId;
            this.sContent = content;
            this.sTime = time;
            this.sKind = kind;
            Log.d("ReceiveMessageThread","Constructor 안,no: "+sNo +", sFriend: "+sFriendId + ", content: "+sContent + ", time: " + time + ", kind: " + kind);
            for(int i=0;i<chatRoomList.size();i++){
                Log.d("챗룸리스트목록",chatRoomList.get(i));
            }
        }

        @Override
        public void run() {



            if(sKind == 1) {

                db.insertMessageData(userId,sNo,sFriendId, sContent, sTime, 1);

                if(boundStart) {
                    if(sNo == 0 ) {
                        if(boundedNo == 0 && boundedFriendId.equals(sFriendId)) {
                            db.updateChatRoomData(sNo, sFriendId, sTime);
                            sendRead(sNo,mCallback.getFriendId(),sTime);
                        }
                    }else{
                        if(boundedNo == sNo) {
                            db.updateChatRoomData(sNo, sFriendId, sTime);
                            sendRead(sNo,mCallback.getFriendId(),sTime);
                        }
                    }
                }

                if (sNo == 0 && !chatRoomList.contains(sFriendId)) {

                    getFriendThread gft = new getFriendThread(sFriendId, sContent, sTime);
                    Log.d("gft.start전", chatRoomList.contains(sFriendId) + "");
                    gft.start();


                    Log.d("gft.start후", chatRoomList.contains(sFriendId) + "");


                } else if (sNo > 0 && !chatRoomList.contains(sNo + "")) {

                    getGroupThread ggt = new getGroupThread(sNo, sFriendId, sContent, sTime);
                    ggt.start();

                } else {



                    if (boundCheck == true) {

                        if(sNo == 0 ) {

                            if(boundedNo == 0 && boundedFriendId.equals(sFriendId)) {
                                Log.d("mCallback.recvData1",sFriendId+"###"+sContent+"####"+sNo);
                                mCallback.recvData(sFriendId, sContent, sTime);
                                Log.d("mCallback.recvData2",sFriendId+"###"+sContent+"####"+sNo);
                            }



                        }else{
                            if(boundedNo == sNo) {
                                mCallback.recvData(sFriendId, sContent, sTime);
                                Log.d("mCallback.recvData",sFriendId+"###"+sContent+"####"+sNo);
                            }
                        }

                    }else{

                        NotificationAlarm(sFriendId,sNo,"#없음",sContent);

                    }

                    if (boundCheck_2 == true) {
                        mCallback_2.recvData();
                    }


                }



            }else if(sKind == 2){

                db.updateChatFriendData(sNo,sFriendId,sTime);
                Log.d("도태",sNo+"####"+sFriendId+"###"+boundStart+"###"+boundedNo+"###"+boundedFriendId);
                if(boundStart) {

                    if(sNo == 0 ) {
                        if(boundedNo == 0 && boundedFriendId.equals(sFriendId)) {
                            mCallback.recvUpdate();
                        }
                    }else{
                        if(boundedNo == sNo) {
                            mCallback.recvUpdate();
                            Log.d("도태2",sNo+"####"+sFriendId+"###"+boundStart);
                        }
                    }

                }
            }else if(sKind == 10){

                if(boundCheck) {
                    if(sNo == 0 ) {
                        if(boundedNo == 0 && boundedFriendId.equals(sFriendId)) {
                            mCallback.receivePath(sContent);
                        }
                    }else{
                        if(boundedNo == sNo) {
                            mCallback.receivePath(sContent);
                        }
                    }

                }


            }else if(sKind == 11){


                if(boundCheck) {
                    if(sNo == 0 ) {
                        if(boundedNo == 0 && boundedFriendId.equals(sFriendId)) {
                            mCallback.receiveClear();
                        }
                    }else{
                        if(boundedNo == sNo) {
                            mCallback.receiveClear();
                        }
                    }

                }


            }else if(sKind == 22){
//                loseReceive();
            }


        }


    }



    public void NotificationAlarm(String friendId, int no, String nickname, String content){

        Intent mAlarmIntent = new Intent(getApplicationContext(), ChatRoomActivity.class);

        mAlarmIntent.putExtra("no",no);
        mAlarmIntent.putExtra("friendId",friendId);

        if(no == 0){
            mAlarmIntent.putExtra("nickname", nickname);
        }else {
            mAlarmIntent.putExtra("nickname", "그룹채팅 "+no);
        }


        PendingIntent mPendingIntent = PendingIntent.getActivity(getApplicationContext(),1,mAlarmIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getApplicationContext())
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentIntent(mPendingIntent)
                        .setContentTitle("캐치마인드메신저")
                        .setContentText(content);


        NotificationManager mNotificationManager =

                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


        mNotificationManager.notify(001, mBuilder.build());


    }


}
