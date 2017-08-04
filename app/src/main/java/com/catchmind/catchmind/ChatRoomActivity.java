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

        Intent GI = getIntent();

        friendId = GI.getStringExtra("friendId");

        Cursor cursor = db.getFriendData(friendId);
        cursor.moveToNext();
        friendNickname = cursor.getString(1);
        friendProfile = cursor.getString(2);

        getSupportActionBar().setTitle(friendId);

        // Initializing ViewPager
        viewPager = (ViewPager) findViewById(R.id.pagerChatRoom);
        mf = new MessageRoomFragment();
        df = new DrawRoomFragment();
        fragmentCommunicator = (FragmentCommunicator) mf;
        ChatRoomPagerAdapter pagerAdapter = new ChatRoomPagerAdapter(getSupportFragmentManager(),mf,df,mPref,friendId);



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
                    String content = msg.getData().getString("content");
                    String time = msg.getData().getString("time");
                    fragmentCommunicator.passData(friendId,friendNickname,friendProfile, content, time,1);
                }else if(msg.what ==2){
                    String content = msg.getData().getString("content");
                    String time = msg.getData().getString("time");
                    fragmentCommunicator.passData("내아아이디","내닉네임","내프로필", content, time, 2);
                }else{

                }


            }
        };


    }

    public interface FragmentCommunicator {

        void passData(String friendId, String nickname, String profile, String content, String date,int type);

    }

    public void passVal(FragmentCommunicator fragmentCommunicator) {
        this.fragmentCommunicator = fragmentCommunicator;

    }

    private ServiceConnection mConnection = new ServiceConnection() {
        // Called when the connection with the service is established
        public void onServiceConnected(ComponentName className, IBinder service) {
            ChatService.ChatServiceBinder binder = (ChatService.ChatServiceBinder) service;
            mService = binder.getService(); //서비스 받아옴
            mService.registerCallback(mCallback); //콜백 등록
            mService.boundCheck = true;
        }

        // Called when the connection with the service disconnects unexpectedly
        public void onServiceDisconnected(ComponentName className) {
            mService = null;
        }
    };


    private ChatService.ICallback mCallback = new ChatService.ICallback() {

        public void recvData(String friendId,String content,String time) {

                        Message message= Message.obtain();
                        message.what = 1;
                        Bundle bundle = new Bundle();
                        bundle.putString("friendId",friendId);
                        bundle.putString("content",content);
                        bundle.putString("time",time);
                        message.setData(bundle);
                        handler.sendMessage(message);

        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mService.boundCheck = false;
        unbindService(mConnection);
    }





    public void sendMessage(View v){


        long now = System.currentTimeMillis();
        Date nowdate = new Date(now);
        SimpleDateFormat sdfNow = new SimpleDateFormat("HH:mm");
        String time = sdfNow.format(nowdate);

        String et = sendcontent.getText().toString();

        db.insertMessageData(userId,friendId,et,time,2);

        mService.sendMessage(friendId,et,time);

        Message message= Message.obtain();
        message.what = 2;

        Bundle bundle = new Bundle();
        bundle.putString("content",et);
        bundle.putString("time",time);

        message.setData(bundle);

        handler.sendMessage(message);

    }


    public String getUserId() {
        return userId;
    }






}
