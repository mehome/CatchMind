package com.catchmind.catchmind;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TabLayout;

import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;



/**
 * Created by sonsch94 on 2017-07-19.
 */

public class ChatRoomActivity extends AppCompatActivity {


    private ViewPager viewPager;
    String tmpTitle;
    Toolbar toolbar;
    Socket socket;
    EditText sendcontent;
    final private static String LOG = "ChatRoomActivity";
    public Handler handler;
    MessageRoomFragment mf;
    DrawRoomFragment df;
    FragmentCommunicator fragmentCommunicator;
    public String sendName;
    public String sendContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatroom);

        sendcontent = (EditText)findViewById(R.id.messageContent);

        // Adding Toolbar to the activity
        toolbar = (Toolbar) findViewById(R.id.toolbarChatRoom);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true); //커스터마이징 하기 위해 필요
//        actionBar.setDisplayShowTitleEnabled(false);

        Intent GI = getIntent();

        tmpTitle = GI.getStringExtra("tmpTitle");
        Log.d("고유닉",tmpTitle);

        getSupportActionBar().setTitle(tmpTitle);

        ConnectTask startConnect = new ConnectTask();
        startConnect.execute();

        // Initializing ViewPager
        viewPager = (ViewPager) findViewById(R.id.pagerChatRoom);
        mf = new MessageRoomFragment();
        df = new DrawRoomFragment();
        fragmentCommunicator = (FragmentCommunicator) mf;
        ChatRoomPagerAdapter pagerAdapter = new ChatRoomPagerAdapter(getSupportFragmentManager(),mf,df);


        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });




         handler = new Handler(){
            @Override
            public void handleMessage(Message msg){

//                String response = (String)msg.obj;
//                Toast.makeText(getApplicationContext(),response,Toast.LENGTH_SHORT).show();
                if(msg.what == 1) {
                    fragmentCommunicator.passData(sendName, sendContent, 1);
                }else{
                    String sendData = (String) msg.obj;
                    fragmentCommunicator.passData("나자신", sendData, 2);
                }
                     Log.d("핸들러","핸들러안"+sendName+sendContent);

            }
        };


    }

    public interface FragmentCommunicator {

        public void passData(String name, String content, int type);

    }

    public void passVal(FragmentCommunicator fragmentCommunicator) {
        this.fragmentCommunicator = fragmentCommunicator;

    }


    public class SendThread extends Thread {

        String sendmsg;

        OutputStream sender ;

        public SendThread(Socket threadSocket,String msg) {

            this.sendmsg = msg;

            try {
                this.sender = threadSocket.getOutputStream();
            }catch (IOException e){
                e.printStackTrace();
            }

            Log.d("내용물1t",sendmsg);

        }

        @Override
        public void run() {

//
//            JSONObject sendjson = new JSONObject();
//
//            try{
//
//                sendjson.put("name","손순철");
//                sendjson.put("content",sendmsg);
//                sendmsg = sendjson.toString();
//
//            }catch (JSONException e){
//
//            }


            org.json.simple.JSONObject obj = new org.json.simple.JSONObject();
            obj.put("name",tmpTitle);
            obj.put("content",this.sendmsg);
            this.sendmsg = obj.toJSONString() + System.getProperty("line.separator");

            try {

                byte[] data ;
                data = this.sendmsg.getBytes();
                sender.write(data, 0, data.length);

            }catch (IOException e){

            }
        }
    }


    public class ReceiveThread extends Thread {

        String response;
        InputStream receiver;
        BufferedReader br;

        public ReceiveThread(Socket threadsocket) {
            try{
                this.receiver = threadsocket.getInputStream();
                this.br = new BufferedReader(new InputStreamReader(receiver));
            }catch (Exception e){

            }
        }


        @Override
        public void run() {

            while(true) {
                try {
                    while (receiver != null) {

                        response = br.readLine();

//                        Message message= Message.obtain();
//                        message.obj = response;
//                        handler.sendMessage(message);

                        String name = null;
                        String content = null;

                        try {
                            JSONObject obj = new JSONObject(response);
                            name = obj.getString("name");
                            content = obj.getString("content");

                        }catch(JSONException e){

                        }

                        sendName = name;
                        sendContent = content;

                        response = "이름은 " +name+"이고 내용은 "+content;
                        Message message= Message.obtain();
                        message.what = 1;
                        message.obj = response;
                        handler.sendMessage(message);
                        Log.d("핸들러","리시브안"+response);

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }


    public void sendMessage(View v){
        String et = sendcontent.getText().toString();
        SendThread startSend = new SendThread(socket,et);
        startSend.start();
        sendcontent.setText("");
        Log.d("소켓내용물",socket.toString());
        Message message= Message.obtain();
        message.what = 2;
        message.obj = et;
        handler.sendMessage(message);
    }

    public void postConnect(){
        ReceiveThread startReceive = new ReceiveThread(socket);
        startReceive.start();
    }

    public String getNickname() {
        return tmpTitle;
    }



    public class ConnectTask extends AsyncTask<Void, Void, Void> {

        String dstAddress;
        int dstPort;


        public ConnectTask() {
            this.dstAddress = "115.71.233.144";
            this.dstPort = 5000;
            Log.d(LOG+"뭐지대체",dstAddress+dstPort);


        }

        @Override
        protected Void doInBackground(Void... arg0) {

            try {
                socket = new Socket(dstAddress, 5000);


            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            try {
                OutputStream sender = socket.getOutputStream();
                byte[] data ;
                String sendData = tmpTitle + System.getProperty("line.separator");
                data = sendData.getBytes();
                sender.write(data, 0, data.length);

            }catch (IOException e){
                e.printStackTrace();
            }

//            try {
//
//
//                byte[] data ;
//
//                String sendmsg = "zzz";
//
//                JSONObject sendjson = new JSONObject();
//
//                try{
//
//                    sendjson.put("name",tmpTitle);
//                    sendjson.put("content",sendmsg);
//                    sendmsg = sendjson.toString()+System.getProperty("line.separator");
//                    Log.d("진짜",sendmsg);
//
//                }catch (JSONException e){
//
//                }
//
////                org.json.simple.JSONObject obj = new org.json.simple.JSONObject();
////                obj.put("name","손순철");
////                obj.put("content",sendmsg);
////                sendmsg = obj.toJSONString();
////                sendmsg = sendmsg + System.getProperty("line.separator");
////                Log.d("진짜",sendmsg);
//
//                data = sendmsg.getBytes();
//                OutputStream sender = socket.getOutputStream();
//                sender.write(data, 0, data.length);
//
//            }catch (IOException e){
//
//            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            postConnect();
            Log.d("소켓",socket.toString());
        }

    }

//    public class ReceiveTask extends AsyncTask<Void, Void, Void> {
//
//
//        String response;
//        Socket socket;
////        DataInputStream input;
//        InputStream receiver;
//
//        public ReceiveTask(Socket socket) {
//            this.socket = socket;
//            try{
//                this.receiver = socket.getInputStream();
//            }catch (Exception e){
//
//            }
//        }
//
//        @Override
//        protected Void doInBackground(Void... arg0) {
//
//            try {
//                while (receiver != null) {
//                    byte[] data = new byte[1024];
//                    receiver.read(data,0,1024);
//
//                    response = new String(data);
//
////                    postReceive(response);
//
//                }
//            }catch(IOException e){
//                e.printStackTrace();
//            }
//
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Void result) {
//            super.onPostExecute(result);
//        }
//
//    }


//    public class SendTask extends AsyncTask<Void, Void, Void> {
//
//
//        String sendmsg;
////        Socket socket;
//        OutputStream sender ;
//        DataOutputStream output;
//
//        public SendTask(String msg) {
////            this.socket = socket2;
//            this.sendmsg = msg;
////            try {
////                this.sender = socket.getOutputStream();
////                this.output = new DataOutputStream(this.sender);
////            }catch (IOException e){
////                e.printStackTrace();
////            }
//            Log.d("뭐지대체",msg+"");
//            Log.d("내용물1",sendmsg);
//
//        }
//
//        @Override
//        protected Void doInBackground(Void... arg0) {
//
//            JSONObject sendjson = new JSONObject();
//            try{
//
//                sendjson.put("name","aaa");
//                sendjson.put("content",sendmsg);
//                sendmsg = sendjson.toString();
////
////            }catch (JSONException e){
////
////            }
//            Log.d("내용물2",sendmsg);
////            try {
////                byte[] data ;
////                data = this.sendmsg.getBytes();
////                Log.d("내용물3",sendmsg);
////                sender.write(data, 0, data.length);
//////                this.output.writeU("zzzzz");
////            }catch (IOException e){
////
////            }
//
//
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Void result) {
//            super.onPostExecute(result);
//        }
//
//    }
//
//
//    public void postReceive(String response){
//
//        Toast.makeText(this,response,Toast.LENGTH_SHORT).show();
//
//    }


}
