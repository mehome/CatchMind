package com.catchmind.catchmind;

import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.internal.NavigationMenuView;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;

import android.support.v4.app.FragmentManager;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;

import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
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
    String userNickname;
    public MyDatabaseOpenHelper db;
    public int no;
    public HashMap<String,String> NickHash = new HashMap<>();
    public HashMap<String,String> ProfileHash = new HashMap<>();
    BroadcastReceiver NetworkChangeUpdater;
    public ImageButton plusBtn;
    public Button drawModeBtn;
    public Button sendMsgBtn;
    public Button drawChatBtn;
    public ImageButton alarmActive;
    DrawerLayout drawer;

    public int delPosition;

    public static final int MakeGroupActivity = 6839;

    public static final int DeleteImage = 3102;

    public static final int DeleteMessage = 2013;



    MemberListAdapter memberListAdapter;
    ArrayList<MemberListItem> ListData;

    NavigationView navigationView;

    final String upLoadServerUri = "http://vnschat.vps.phps.kr/SendImage.php";
    private static final int PICK_FROM_CAMERA = 0;
    private static final int PICK_FROM_ALBUM = 1;
    private Uri mImageCaptureUri;

    String alarmKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatroom_nav);


        sendcontent = (EditText)findViewById(R.id.messageContent);

        // Adding Toolbar to the activity
        toolbar = (Toolbar) findViewById(R.id.toolbarChatRoom);
        plusBtn = (ImageButton) findViewById(R.id.plus_btn);
        drawModeBtn = (Button) findViewById(R.id.drawMode_btn);
        sendMsgBtn = (Button) findViewById(R.id.SendMsgBtn);
        drawChatBtn = (Button) findViewById(R.id.drawChatBtn);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true); //커스터마이징 하기 위해 필요
//        actionBar.setDisplayShowTitleEnabled(false);


        db = new MyDatabaseOpenHelper(this,"catchMind",null,1);
        mPref = getSharedPreferences("login",MODE_PRIVATE);
        editor = mPref.edit();


        userId = mPref.getString("userId","아이디없음");
        userNickname = mPref.getString("nickname","닉없음");
        Log.d("chatroomId",userId);
        Intent GI = getIntent();


        friendId = GI.getStringExtra("friendId");
        no = GI.getIntExtra("no",0);
        if(friendId.equals("noti")){
            getFriendId(no);
        }


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
                    drawChatBtn.setVisibility(View.GONE);
                    sendMsgBtn.setVisibility(View.VISIBLE);

                }else if(position == 1){
                    ChatRoomViewPager.DrawMode = false;
                    plusBtn.setVisibility(View.GONE);
                    drawModeBtn.setVisibility(View.VISIBLE);
                    sendMsgBtn.setVisibility(View.GONE);
                    drawChatBtn.setVisibility(View.VISIBLE);
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
                }else if(msg.what==99){
                    memberListAdapter.notifyDataSetChanged();
                }else if(msg.what==88){

                    String friendId = msg.getData().getString("friendId");
                    String content = msg.getData().getString("content");
                    String nickname;
                    if(no == 0) {
                        nickname = friendNickname;
                    }else{
                        nickname = NickHash.get(friendId);
                    }
                    drawCommunicator.drawChat(nickname,content);
                }else if(msg.what==51){

                    if(no ==0) {
                        String content = msg.getData().getString("content");
                        long time = msg.getData().getLong("time");
                        fragmentCommunicator.passData(friendId, friendNickname, friendProfile, content, time, 51);
                    }else{
                        String friendId = msg.getData().getString("friendId");
                        String content = msg.getData().getString("content");
                        long time = msg.getData().getLong("time");
                        fragmentCommunicator.passData(friendId, NickHash.get(friendId), ProfileHash.get(friendId), content, time, 51);
                    }

                }else if(msg.what==52){

                    String friendId = msg.getData().getString("friendId");
                    String content = msg.getData().getString("content");
                    long time = msg.getData().getLong("time");
                    fragmentCommunicator.passData("내아이디","내닉네임","내프로필", content, time, 52);

                }else if(msg.what == 365){
                    fragmentCommunicator.bottomSelect();
                }


            }
        };


        attachKeyboardListeners();

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        ListView lv = (ListView) findViewById(R.id.memberList);

        if(no != 0) {

            View header = getLayoutInflater().inflate(R.layout.member_invite_header, null, false);

            header.setOnClickListener(mClickListener);

            lv.addHeaderView(header);

        }

        ListData = new ArrayList<>();

        Cursor cursor;

        if(no == 0 ){
            cursor = db.getChatFriendListDataWithMe(friendId);
        }else {
            cursor = db.getChatFriendListByNo(no);
        }

        while(cursor.moveToNext()){
            MemberListItem addItem = new MemberListItem(cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4));
            ListData.add(addItem);
        }


        memberListAdapter = new MemberListAdapter(this,ListData);

        lv.setAdapter(memberListAdapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("아이템",""+position);
            }
        });

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        alarmActive = (ImageButton) findViewById(R.id.alarmImageBtn);

        if(no == 0){
            alarmKey = friendId;
        }else{
            alarmKey = no + "";
        }
        if(!mPref.getBoolean(alarmKey,true)){
            alarmActive.setBackgroundResource(R.drawable.alarm_disable_icon);
        }else{
            alarmActive.setBackgroundResource(R.drawable.alarm_active_icon);
        }


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

    public void activeAlarm(View v){

        if(mPref.getBoolean(alarmKey,true)){
            editor.putBoolean(alarmKey,false);
            editor.commit();
            alarmActive.setBackgroundResource(R.drawable.alarm_disable_icon);

        }else{

            editor.putBoolean(alarmKey,true);
            editor.commit();
            alarmActive.setBackgroundResource(R.drawable.alarm_active_icon);


        }
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

    public void ResetMemberList(){
        memberListAdapter.clearList();

        Cursor cursor = db.getChatFriendListByNo(no);

        while(cursor.moveToNext()){
            MemberListItem addItem = new MemberListItem(cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4));
            memberListAdapter.addMemberItem(addItem);
            Log.d("귀여워",cursor.getString(1));
        }

        Message message= Message.obtain();
        message.what = 99;

        handler.sendMessage(message);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){

            if(requestCode == MakeGroupActivity){
                long now = System.currentTimeMillis();
                String content = data.getExtras().getString("content");
                String inviteId = data.getExtras().getString("inviteId");

                mService.sendInvite(no,friendId,content,now,inviteId);

            }else if(requestCode == PICK_FROM_CAMERA){

                try {

//                    String ex_storage = Environment.getExternalStorageDirectory().getAbsolutePath();
//                    String imgpath = ex_storage + "/tmp.png";


                    String imgpath = data.getExtras().getString("CustomPath");

                    if(imgpath.equals("none")) {
                        Toast.makeText(this,"사진촬영 실패",Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Log.d("이미지경로",imgpath);


                    ImageSendThread ist = new ImageSendThread(imgpath);
                    ist.start();

                    ist.join();

                    SetBottomThread sbt = new SetBottomThread();
                    sbt.start();

                }catch(Exception e){
                    e.printStackTrace();
                }


            }else if(requestCode == PICK_FROM_ALBUM ){

                try {

                    Log.d("이미지경로",getPath(data.getData()));

                    ImageSendThread ist = new ImageSendThread(getPath(data.getData()));

                    ist.start();

                    ist.join();

                    SetBottomThread sbt = new SetBottomThread();
                    sbt.start();



                }catch(Exception e){
                    e.printStackTrace();
                }

            }else if(requestCode == DeleteImage){





                int position = data.getExtras().getInt("position");
                fragmentCommunicator.deleteMessage(position);



            }else if(requestCode == DeleteMessage){


                delPosition = data.getExtras().getInt("position");
                String type = data.getExtras().getString("type");

                if(type.equals("del")) {

                    DialogInterface.OnClickListener deleteListener = new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            fragmentCommunicator.deleteMessage(delPosition);
                        }

                    };


                    DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }

                    };


                    AlertDialog dialog = new AlertDialog.Builder(this)
                            .setMessage("선택한 메시지를 삭제하시겠습니까 \n \n 삭제한 메시지는 내 채팅방에서만 적용되며 상대방의 채팅방에서는 삭제되지 않습니다.")
                            .setPositiveButton("확인", deleteListener)
                            .setNegativeButton("취소", cancelListener)
                            .create();


                    dialog.show();


                    Button deleteBtn = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                    deleteBtn.setTextColor(Color.BLACK);

                    Button cancelBtn = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                    cancelBtn.setTextColor(Color.BLACK);


                }else if(type.equals("copy")){


                    String subType = data.getExtras().getString("subType");

                    if(subType.equals("text")) {

                        String content = data.getExtras().getString("content");

                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("catchMind", content);
                        clipboard.setPrimaryClip(clip);
                    }


                }

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
            Log.d("리시브시발",cursor.getString(1));
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
        void deleteMessage(int position);
        void bottomSelect();

    }

    public interface DrawCommunicator {

        void receivePath(String PATH);
        void resizeSketchBook();
        void MinusWidth();
        void PlusWidth();
        void receiveClear();
        void drawChat(String Nickname,String Content);

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

        public void sendInviteMark(String inviteId,String content,long time,boolean resetMemberList){

            if(resetMemberList) {
                ResetMemberList();
                try {
                    JSONArray jsonArray = new JSONArray(friendId);
                    JSONArray addArray = new JSONArray(inviteId);
                    for(int i=0;i<addArray.length();i++){
                        jsonArray.put(addArray.getString(i));
                    }
                    friendId = jsonArray.toString();
                    Log.d("집중",friendId);
                }catch (JSONException e){
                    e.printStackTrace();
                }
                ResetHash();
            }

            Message message= Message.obtain();
            message.what = 3;

            Bundle bundle = new Bundle();
            bundle.putString("content",content);
            bundle.putLong("time",time);

            message.setData(bundle);

            handler.sendMessage(message);
        }

        @Override
        public void sendExitMark(String sFriendId, String content, long time) {

            ResetMemberList();

            try {
                JSONParser parser = new JSONParser();
                org.json.simple.JSONArray jarray = (org.json.simple.JSONArray) parser.parse(friendId);
                jarray.remove(sFriendId);
                friendId = jarray.toString();
            }catch (ParseException e){
                e.printStackTrace();
            }

            ResetHash();

            Message message= Message.obtain();
            message.what = 3;

            Bundle bundle = new Bundle();
            bundle.putString("content",content);
            bundle.putLong("time",time);

            message.setData(bundle);

            handler.sendMessage(message);
        }

        @Override
        public void sendImageMark(String friendId, String content, long time ,int kind) {


            Message message= Message.obtain();
            message.what = kind;

            Bundle bundle = new Bundle();
            bundle.putString("content",content);
            bundle.putLong("time",time);
            bundle.putString("friendId",friendId);

            message.setData(bundle);

            handler.sendMessage(message);



        }

        public void resetHash(){
            ResetHash();
            ResetMemberList();
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

        @Override
        public void receiveDrawChat(String friendId, String content) {
            Message message= Message.obtain();
            message.what = 88;

            Bundle bundle = new Bundle();
            bundle.putString("content",content);
            bundle.putString("friendId",friendId);

            message.setData(bundle);

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
            if(drawer.isDrawerOpen(navigationView)){
                drawer.closeDrawer(navigationView);
            }else {
                finish(); // close this activity and return to preview activity (if there is any)
            }
        }else if(item.getItemId() == R.id.drawer_menu_icon){
            if(drawer.isDrawerOpen(navigationView)){
                drawer.closeDrawer(navigationView);
            }else {
                drawer.openDrawer(navigationView);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    public void ImageSendBtn(View v){
        DialogInterface.OnClickListener cameraListener = new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which){
                doTakePhotoAction();
            }

        };

        DialogInterface.OnClickListener albumListener = new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which){
                doTakeAlbumAction();

            }

        };


        DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener(){

            @Override public void onClick(DialogInterface dialog, int which){
                dialog.dismiss();
            }

        };




        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("이미지 전송")
                .setPositiveButton("사진촬영", cameraListener)
                .setNeutralButton("취소", cancelListener)
                .setNegativeButton("앨범선택", albumListener)
                .create();

        dialog.show();

        Button pbtn = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        pbtn.setTextColor(Color.BLACK);
        Button neubtn = dialog.getButton(DialogInterface.BUTTON_NEUTRAL);
        neubtn.setTextColor(Color.BLACK);
        Button negbtn = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        negbtn.setTextColor(Color.BLACK);
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


    public void drawChat(View v){
        String et = sendcontent.getText().toString();
        drawCommunicator.drawChat(userNickname,et);
        sendcontent.setText("");
        mService.sendDrawChat(no,friendId,et,0);
    }

    public void exitRoom(View v){

        DialogInterface.OnClickListener exitListener = new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which){
                exitRoom();
            }

        };


        DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener(){

            @Override public void onClick(DialogInterface dialog, int which){
                dialog.dismiss();
            }

        };




        AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage("채팅방에서 나가기를 하면 대화 내용 및 채팅목록에서 모두 삭제됩니다.\n채팅방에서 나가시겠습니까?")
                .setPositiveButton("확인", exitListener)
                .setNegativeButton("취소", cancelListener)
                .create();

        dialog.show();

        Button exitBtn = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        exitBtn.setTextColor(Color.BLACK);

        Button cancelBtn = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        cancelBtn.setTextColor(Color.BLACK);

    }

    public void exitRoom(){

        db.deleteRoom(no,friendId);
        db.deleteChatFriendAll(no,friendId);
        db.deleteMessageData(no,friendId);
        String content = userNickname + "님이 나갔습니다";
        long now = System.currentTimeMillis();
        mService.sendExit(no,friendId,content,now);
        finish();

    }

    public void getFriendId(int no){
        Cursor cursor = db.getChatFriendListByNo(no);
        JSONArray idArray = new JSONArray();

        while(cursor.moveToNext()){
            idArray.put(cursor.getString(1));
        }

        friendId = idArray.toString();
    }




    public int uploadFile(String sourceFileUri) {

        Log.d("이미지업로드시작CRA",sourceFileUri);
        String fileName = sourceFileUri;

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
//        File sourceFile = new File(sourceFileUri);

        String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/tmp.png";

        Bitmap b= BitmapFactory.decodeFile(sourceFileUri);

        Log.d("뭘기대",b.getWidth()+"###"+b.getHeight());

        float height = 400 * (float)b.getHeight() /  (float)b.getWidth();

        Bitmap out = Bitmap.createScaledBitmap(b, 400, (int)height, false);


        File sourceFile = new File(path);
        FileOutputStream fOut;



        try {

            Log.d("이미지새파일경로1",sourceFile.getAbsolutePath());
            fOut = new FileOutputStream(sourceFile);
            Log.d("이미지새파일경로2",sourceFile.getAbsolutePath());
            out.compress(Bitmap.CompressFormat.PNG, 100, fOut);

        } catch (Exception e) {

        }



        if (!sourceFile.isFile()) {

            Log.d("이미지경로에없음","경로에없나?");
//            dialog.dismiss();

            return 0;

        }else{

            int serverResponseCode = 123;

            try {

                // open a URL connection to the Servlet
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                URL url = new URL(upLoadServerUri);
                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("uploaded_file", fileName);

                dos = new DataOutputStream(conn.getOutputStream());
                dos.writeBytes(twoHyphens + boundary + lineEnd);


                long now = System.currentTimeMillis();
                String imageName = userId + "_" + now + ".png";


                dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                        + imageName + "\"" + lineEnd);

                dos.writeBytes(lineEnd);
                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available();

                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {

                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                }

                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Responses from the server (code and message)
                serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();

                Log.d("이미지uploadFile", "HTTP Response is : "
                        + serverResponseMessage + ": " + serverResponseCode);


                //close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();


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
                String data = buff.toString().trim();
                Log.d("이미지성공실패",data);

                if(data.equals("OK")){
                    mService.sendImage(no,friendId,imageName,now);
                }


            } catch (MalformedURLException ex) {


                ex.printStackTrace();



                Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
            } catch (Exception e) {

                e.printStackTrace();


            }

//            dialog.dismiss();
            return serverResponseCode;

        } // End else block

    }




    public class ImageSendThread extends Thread {

        public String filePath;

        public ImageSendThread (String uri){
            this.filePath = uri;
        }

        @Override
        public void run() {

            uploadFile(filePath);

        }


    }


//    public void imageSend(View v){
//
//        DialogInterface.OnClickListener cameraListener = new DialogInterface.OnClickListener(){
//
//            @Override
//            public void onClick(DialogInterface dialog, int which){
//                doTakePhotoAction();
//            }
//
//        };
//
//        DialogInterface.OnClickListener albumListener = new DialogInterface.OnClickListener(){
//
//            @Override
//            public void onClick(DialogInterface dialog, int which){
//                doTakeAlbumAction();
//
//            }
//
//        };
//
//
//        DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener(){
//
//            @Override public void onClick(DialogInterface dialog, int which){
//                dialog.dismiss();
//            }
//
//        };
//
//
//
//
//        AlertDialog dialog = new AlertDialog.Builder(this)
//                .setTitle("이미지 전송")
//                .setPositiveButton("사진촬영", cameraListener)
//                .setNeutralButton("취소", cancelListener)
//                .setNegativeButton("앨범선택", albumListener)
//                .create();
//
//        dialog.show();
//
//        Button pbtn = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
//        pbtn.setTextColor(Color.BLACK);
//        Button neubtn = dialog.getButton(DialogInterface.BUTTON_NEUTRAL);
//        neubtn.setTextColor(Color.BLACK);
//        Button negbtn = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
//        negbtn.setTextColor(Color.BLACK);
//
//
//    }


    public void doTakePhotoAction(){

//        Intent intent = new Intent (MediaStore.ACTION_IMAGE_CAPTURE);
//
//        String url = "tmp" + ".png";
//
//        mImageCaptureUri = FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName()+".provider", new File(Environment.getExternalStorageDirectory(), url));
//
//        Log.d("사진_doTakePhoto", Environment.getExternalStorageState().toString());
//        Log.d("사진_doTakePhoto", mImageCaptureUri.toString());
//
//
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
//        startActivityForResult(intent, PICK_FROM_CAMERA);


        Intent intent = new Intent (this, CustomCameraActivity.class);

        startActivityForResult(intent, PICK_FROM_CAMERA);

    }

    public void doTakeAlbumAction(){

        Intent intent = new Intent (Intent.ACTION_PICK);
//        intent.setType (MediaStore.Images.Media.CONTENT_TYPE);
        intent.setType ("image/*");
        startActivityForResult(intent, PICK_FROM_ALBUM);

    }

    public String getPath(Uri uri) {

        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();

        return cursor.getString(column_index);
    }




    public class SetBottomThread extends Thread {


        public SetBottomThread (){

        }

        @Override
        public void run() {

            try {

                Thread.sleep(700);

                Message message= Message.obtain();
                message.what = 365;

                handler.sendMessage(message);


            }catch (InterruptedException e){
                e.printStackTrace();
            }

        }


    }

}
