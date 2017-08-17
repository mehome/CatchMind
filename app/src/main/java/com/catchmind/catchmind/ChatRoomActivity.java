package com.catchmind.catchmind;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.design.widget.TabLayout;

import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
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
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;


/**
 * Created by sonsch94 on 2017-07-19.
 */

public class ChatRoomActivity extends AppCompatActivity {


    private ViewPager viewPager;
    String friendId;
    String friendNickname;
    String friendProfile;
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
    private ChatService mService;
    public SharedPreferences mPref;
    public SharedPreferences.Editor editor;
    String userId;
    public MyDatabaseOpenHelper db;
    public int no;
    public HashMap<String,String> NickHash = new HashMap<>();
    public HashMap<String,String> ProfileHash = new HashMap<>();


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

        db = new MyDatabaseOpenHelper(this,"catchMind",null,1);
        mPref = getSharedPreferences("login",MODE_PRIVATE);
        editor = mPref.edit();

        userId = mPref.getString("userId","아이디없음");
        Log.d("chatroomId",userId);
        Intent GI = getIntent();

        friendId = GI.getStringExtra("friendId");
        no = GI.getIntExtra("no",0);
        Log.d("chatroomId2",friendId);
        String nickname = GI.getStringExtra("nickname");

        if(no == 0) {
            Cursor cursor = db.getFriendData(friendId);
            cursor.moveToNext();
            friendNickname = cursor.getString(1);
            friendProfile = cursor.getString(2);
        }else {
            ResetHash();
        }

        getSupportActionBar().setTitle(nickname);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Initializing ViewPager
        viewPager = (ViewPager) findViewById(R.id.pagerChatRoom);
        mf = new MessageRoomFragment();
        df = new DrawRoomFragment();
        fragmentCommunicator = (FragmentCommunicator) mf;
        ChatRoomPagerAdapter pagerAdapter = new ChatRoomPagerAdapter(getSupportFragmentManager(),mf,df,mPref,friendId,no);

        Log.d("chatRoomActivity",userId+"###"+no+"###"+friendId);

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

        Intent serviceIntent = new Intent(this, ChatService.class);
        bindService(serviceIntent, mConnection, this.BIND_AUTO_CREATE);


         handler = new Handler(){
            @Override
            public void handleMessage(Message msg){


                if(msg.what == 1) {

                    if(no ==0) {
                        String content = msg.getData().getString("content");
                        long time = msg.getData().getLong("time");
                        fragmentCommunicator.passData(friendId, friendNickname, friendProfile, content, time, 1);
                    }else{
                        String friendId = msg.getData().getString("friendId");
                        String content = msg.getData().getString("content");
                        long time = msg.getData().getLong("time");
                        fragmentCommunicator.passData(friendId, NickHash.get(friendId), ProfileHash.get(friendId), content, time, 1);
                    }

                }else if(msg.what ==2){
                    String content = msg.getData().getString("content");
                    long time = msg.getData().getLong("time");
                    fragmentCommunicator.passData("내아아이디","내닉네임","내프로필", content, time, 2);
                }else if(msg.what==3){
                    fragmentCommunicator.alertChange();
                }


            }
        };


    }

    public void ResetHash(){
        NickHash = new HashMap<>();
        ProfileHash = new HashMap<>();
        Cursor cursor = db.getChatFriendListByNo(no);
        while(cursor.moveToNext()){
            NickHash.put(cursor.getString(1),cursor.getString(2));
            ProfileHash.put(cursor.getString(1),cursor.getString(3));
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        db.updateChatRoomData(no,friendId,System.currentTimeMillis());
        if(mService != null) {
            mService.boundStart = true;
            long now = System.currentTimeMillis();
            mService.sendRead(no, friendId, now);
        }
        fragmentCommunicator.alertChange();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mService.boundStart = false;
    }

    public interface FragmentCommunicator {

        void passData(String friendId, String nickname, String profile, String content, long time,int type);
        void alertChange();
        void changeNo(int sNo);
    }

//    public void passVal(FragmentCommunicator fragmentCommunicator) {
//        this.fragmentCommunicator = fragmentCommunicator;
//
//    }

    private ServiceConnection mConnection = new ServiceConnection() {
        // Called when the connection with the service is established
        public void onServiceConnected(ComponentName className, IBinder service) {
            ChatService.ChatServiceBinder binder = (ChatService.ChatServiceBinder) service;
            mService = binder.getService(); //서비스 받아옴
            mService.registerCallback(mCallback); //콜백 등록
            mService.boundCheck = true;
            mService.boundStart = true;
            mService.boundedNo = no;
            mService.boundedFriendId = friendId;
            long now = System.currentTimeMillis();
            mService.sendRead(no, friendId, now);

        }

        // Called when the connection with the service disconnects unexpectedly
        public void onServiceDisconnected(ComponentName className) {
            mService = null;
        }
    };


    private ChatService.ICallback mCallback = new ChatService.ICallback() {

        public void recvData(String friendId,String content,long time) {

                        Message message= Message.obtain();
                        message.what = 1;
                        Bundle bundle = new Bundle();
                        bundle.putString("friendId",friendId);
                        bundle.putString("content",content);
                        bundle.putLong("time",time);
                        message.setData(bundle);
                        handler.sendMessage(message);

        }

        public void recvUpdate(){
            Message message= Message.obtain();
            message.what = 3;
            handler.sendMessage(message);
        }

        public void changeNo(int passNo){
            no = passNo;
            fragmentCommunicator.changeNo(passNo);
        }

        public void sendMessageMark(String content,long time){
            Message message= Message.obtain();
            message.what = 2;

            Bundle bundle = new Bundle();
            bundle.putString("content",content);
            bundle.putLong("time",time);

            message.setData(bundle);

            handler.sendMessage(message);
        }

        public void resetHash(){
            ResetHash();
        }

        public String getFriendId(){
            return friendId;
        }

    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mService.boundCheck = false;
        mService.boundStart = false;
        mService.boundedNo = -1;
        mService.boundedFriendId ="";
        unbindService(mConnection);
    }





    public void sendMessage(View v){


        long now = System.currentTimeMillis();
//        Date nowdate = new Date(now);
//        SimpleDateFormat sdfNow = new SimpleDateFormat("HH:mm");
//        String time = sdfNow.format(nowdate);

        String et = sendcontent.getText().toString();
        sendcontent.setText("");

//        db.insertMessageData(userId,no,friendId,et,now,2,true);
        Log.d("sendMessage,db.insert",userId+"####"+friendId+"####"+et);


        mService.sendMessage(no,friendId,et,now);


//        Message message= Message.obtain();
//        message.what = 2;
//
//        Bundle bundle = new Bundle();
//        bundle.putString("content",et);
//        bundle.putLong("time",now);
//
//        message.setData(bundle);
//
//        handler.sendMessage(message);

    }


    public String getUserId() {
        return userId;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }




}
