package com.catchmind.catchmind;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.internal.NavigationMenuView;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;

import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;

import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;


/**
 * Created by sonsch94 on 2017-07-19.
 */

public class ChatRoomActivity extends BaseActivity implements DrawLine.sendToActivity,NavigationView.OnNavigationItemSelectedListener{



//    private ViewPager viewPager;
    private ChatRoomViewPager viewPager;
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
    DrawCommunicator drawCommunicator;
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
    BroadcastReceiver NetworkChangeUpdater;
    public ImageButton plusBtn;
    public Button drawModeBtn;
    DrawerLayout drawer;

    public static final int MakeGroupActivity = 6839;

    MemberListAdapter memberListAdapter;
    ArrayList<MemberListItem> ListData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatroom_nav);


        sendcontent = (EditText)findViewById(R.id.messageContent);

        // Adding Toolbar to the activity
        toolbar = (Toolbar) findViewById(R.id.toolbarChatRoom);
        plusBtn = (ImageButton) findViewById(R.id.plus_btn);
        drawModeBtn = (Button) findViewById(R.id.drawMode_btn);
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
            Cursor cursor2 = db.getChatFriendData(friendId);

            if(cursor2.getCount() != 0) {
                cursor2.moveToNext();
                if (nickname.equals("#없음")) {
                    nickname = cursor2.getString(2);
                }
            }

            friendProfile = "";
            if(cursor.getCount() != 0) {
                cursor.moveToNext();
                friendProfile = cursor.getString(2);

            }

            friendNickname = nickname;


        }else {
            ResetHash();
        }

        getSupportActionBar().setTitle(nickname);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        NetworkChangeUpdater = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //UI update here
                if (intent != null) {
//                    Toast.makeText(context, "액티비티의 리시버작동!"+intent.toString(), Toast.LENGTH_LONG).show();
                    String networkType = intent.getExtras().getString("wifi");
                    UpdateNetwork(networkType);

                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction("receiver.to.activity.transfer");
        registerReceiver(NetworkChangeUpdater, filter);

        // Initializing ViewPager
        viewPager = (ChatRoomViewPager) findViewById(R.id.pagerChatRoom);
        mf = new MessageRoomFragment();
        df = new DrawRoomFragment();
        fragmentCommunicator = (FragmentCommunicator) mf;
        drawCommunicator = (DrawCommunicator) df;
        ChatRoomPagerAdapter pagerAdapter = new ChatRoomPagerAdapter(getSupportFragmentManager(),mf,df,mPref,friendId,no);

        Log.d("chatRoomActivity",userId+"###"+no+"###"+friendId);

        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                if(position == 0){
                    ChatRoomViewPager.DrawMode = false;
                    drawModeBtn.setVisibility(View.GONE);
                    plusBtn.setVisibility(View.VISIBLE);
                }else if(position == 1){
                    ChatRoomViewPager.DrawMode = false;
                    plusBtn.setVisibility(View.GONE);
                    drawModeBtn.setVisibility(View.VISIBLE);
                }

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
                }else if(msg.what ==3){
                    String content = msg.getData().getString("content");
                    long time = msg.getData().getLong("time");
                    fragmentCommunicator.passData("내아아이디","내닉네임","내프로필", content, time, 3);
                }else if(msg.what==77){
                    fragmentCommunicator.alertChange();
                }else if(msg.what==10){
                    String path = msg.getData().getString("path");
                    drawCommunicator.receivePath(path);
                }else if(msg.what==11){
                    drawCommunicator.receiveClear();
                }


            }
        };


        attachKeyboardListeners();

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ListView lv = (ListView) findViewById(R.id.memberList);

        View header = getLayoutInflater().inflate(R.layout.member_invite_header,null,false);

        header.setOnClickListener(mClickListener);

        lv.addHeaderView(header);

        MemberListItem addItem1 = new MemberListItem("nova","손순철" , "0", "피곤하다");
        MemberListItem addItem2 = new MemberListItem("thdwndrl","송중기" , "0", "행복하자");

        ListData = new ArrayList<>();

        ListData.add(addItem1);
        ListData.add(addItem2);
        memberListAdapter = new MemberListAdapter(this,ListData);

        lv.setAdapter(memberListAdapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("아이템",""+position);
            }
        });

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);



//        Menu menu = navigationView.getMenu();
//        Log.d("미쳐씨발",menu.size()+"");
//        if(no == 0) {
//            MenuItem GroupChat = menu.getItem(0);
//            GroupChat.setVisible(false);
//
//        }else{
//            MenuItem P2PChat = menu.getItem(1);
//            P2PChat.setVisible(false);
//        }

    }

    private View.OnClickListener mClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {


            Intent intentMakeGroup = new Intent(getApplicationContext(),MakeGroupActivity.class);
            intentMakeGroup.putExtra("FCR",true);
            intentMakeGroup.putExtra("friendId",friendId);
            Log.d("설마",friendId);
            startActivityForResult(intentMakeGroup,MakeGroupActivity);


        }
    };


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == MakeGroupActivity){
                long now = System.currentTimeMillis();
                String content = data.getExtras().getString("content");

                Log.d("원피스",data.getExtras().getString("friendId"));
                Log.d("원피스2", content);
                Message message= Message.obtain();
                message.what = 3;

                Bundle bundle = new Bundle();
                bundle.putString("content",content);
                bundle.putLong("time",now);

                message.setData(bundle);

                handler.sendMessage(message);
            }
        }
    }

    @Override
    public void onPostCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onPostCreate(savedInstanceState, persistentState);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, R.string.open_drawer, R.string.close_drawer);

        drawer.addDrawerListener(toggle);
        toggle.syncState();


    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.drawer_menu, menu);
        return true;
    }



    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }

    @Override
    protected void onShowKeyboard(int keyboardHeight) {
        // do things when keyboard is shown
//        Toast.makeText(this,"show",Toast.LENGTH_SHORT).show();
        Log.d("키보드","show"+keyboardHeight);
        drawCommunicator.resizeSketchBook();
    }

    @Override
    protected void onHideKeyboard() {
        // do things when keyboard is hidden
//        Toast.makeText(this,"hide",Toast.LENGTH_SHORT).show();
        Log.d("키보드","hide");
        drawCommunicator.resizeSketchBook();
    }








    public void UpdateNetwork(String type){
        if(type.equals("wifi")) {
            Intent serviceIntent = new Intent(this, ChatService.class);
            bindService(serviceIntent, mConnection, BIND_AUTO_CREATE);
            Log.d("담배Net","UPDATE wifi##"+type);
        }else{
            Intent serviceIntent = new Intent(this, ChatService.class);
            bindService(serviceIntent, mConnection, BIND_AUTO_CREATE);
            Log.d("담배Net","UPDATE nonewifi##"+type);
        }
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

    public interface DrawCommunicator {

        void receivePath(String PATH);
        void resizeSketchBook();
        void MinusWidth();
        void PlusWidth();
        void receiveClear();

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
            message.what =77;
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

        public void sendInviteMark(String content,long time){
            Message message= Message.obtain();
            message.what = 3;

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

        public void resetToolbar() { resetTitle(); }

        public void receivePath(String PATH){

            Message message= Message.obtain();
            message.what = 10;

            Bundle bundle = new Bundle();
            bundle.putString("path",PATH);

            message.setData(bundle);

            handler.sendMessage(message);
        }

        @Override
        public void receiveClear() {
            Message message= Message.obtain();
            message.what = 11;

            handler.sendMessage(message);
        }
    };

    public void resetTitle(){
        getSupportActionBar().setTitle("그룹채팅 "+no);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mService.boundCheck = false;
        mService.boundStart = false;
        mService.boundedNo = -1;
        mService.boundedFriendId ="";
        unbindService(mConnection);
        unregisterReceiver(NetworkChangeUpdater);
        ChatRoomViewPager.DrawMode = false;
    }

    @Override
    public void sendPath(String PATH){
        long now = System.currentTimeMillis();
        mService.sendPATH(no,friendId,PATH,now);
    }

    @Override
    public void sendClear() {
        long now = System.currentTimeMillis();
        mService.sendClear(no,friendId,"just Clear",now);
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
        }else if(item.getItemId() == R.id.drawer_menu_icon){
            drawer.openDrawer(GravityCompat.END);
        }

        return super.onOptionsItemSelected(item);
    }

    public void ImageSendBtn(View v){
        Toast.makeText(this,"ImageSendBtn",Toast.LENGTH_SHORT);
    }

    public void DrawModeBtn(View v){
        if(ChatRoomViewPager.DrawMode){
            drawModeBtn.setBackgroundResource(R.drawable.btn_border);
            ChatRoomViewPager.DrawMode = false;
        }else{
            drawModeBtn.setBackgroundResource(R.drawable.btn_border_active);
            ChatRoomViewPager.DrawMode = true;
        }
    }


    public void minusWidth(View v){
        drawCommunicator.MinusWidth();
    }

    public void plusWidth(View v){
        drawCommunicator.PlusWidth();
    }




}
