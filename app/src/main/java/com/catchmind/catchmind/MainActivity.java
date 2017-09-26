package com.catchmind.catchmind;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TabHost;
import android.widget.Toast;

//import com.facebook.stetho.Stetho;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements TabFragment1.sendToActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    Toolbar toolbar;
    public MyDatabaseOpenHelper db;
    public SharedPreferences mPref;
    public SharedPreferences.Editor editor;
    public int tabPosition;
    public FragmentCommunicator fragmentCommunicator;
    private ChatService mService;
    public Handler handler;
    public String userId;
    public String nickname;
    public static final int MakeGroupActivity = 5409;
    public static final int EditChatRoom = 5828;
    public NetworkChangeReceiver mNCR;
    BroadcastReceiver NetworkChangeUpdater;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        Stetho.initializeWithDefaults(this);
        setContentView(R.layout.activity_main);

        tabPosition = 0;

//        Stetho.initializeWithDefaults(this);

        // Adding Toolbar to the activity
       toolbar = (Toolbar) findViewById(R.id.toolbar);
       setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true); //커스터마이징 하기 위해 필요
//        actionBar.setDisplayShowTitleEnabled(false);
        getSupportActionBar().setTitle("메신저");

        mPref = getSharedPreferences("login",MODE_PRIVATE);
        userId = mPref.getString("userId","닉없음");
        nickname = mPref.getString("nickname","메세지없음");

        db = new MyDatabaseOpenHelper(this,"catchMind",null,1);

        editor = mPref.edit();

        mNCR = new NetworkChangeReceiver();

        IntentFilter NCRfilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);


        registerReceiver(mNCR, NCRfilter);

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



//        Intent serviceIntentMain = new Intent(getApplicationContext(),ChatService.class);
//        startService(serviceIntentMain);


//        Bundle bundle = new Bundle();
//        bundle.putString("nickname",userId);
//        bundle.putString("message",nickname);

        // Initializing the TabLayout
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.profile_icon_act));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.chat_icon_inact));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.setting_icon_inact));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        // Initializing ViewPager
        viewPager = (ViewPager) findViewById(R.id.pager);

        TabFragment1 f1 = new TabFragment1();
        TabFragment2 f2 = new TabFragment2();
        TabFragment3 f3 = new TabFragment3();

        fragmentCommunicator = (FragmentCommunicator) f2;

        // Creating TabPagerAdapter adapter
        TabPagerAdapter pagerAdapter = new TabPagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount(),mPref,f1,f2,f3);
        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        // Set TabSelectedListener
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                tabPosition = position;
                invalidateOptionsMenu();
                viewPager.setCurrentItem(position);

                if(position==0){
                    tab.setIcon(R.drawable.profile_icon_act);
                }else if(position==1){
                    tab.setIcon(R.drawable.chat_icon_act);
                }else if(position==2){
                    tab.setIcon(R.drawable.setting_icon_act);
                }

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                int position = tab.getPosition();


                if(position==0){
                    tab.setIcon(R.drawable.profile_icon_inact);
                }else if(position==1){
                    tab.setIcon(R.drawable.chat_icon_inact);
                }else if(position==2){
                    tab.setIcon(R.drawable.setting_icon_inact);
                }

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }

        });

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg){


                if(msg.what == 1) {
                    fragmentCommunicator.notifyRecvData();
                }else if(msg.what ==2){
                    fragmentCommunicator.changeRoomListFC();
                }


            }
        };


//        Intent youtubeIntent = getIntent();
//
//        Log.d("유튜브",youtubeIntent.toString());

        Intent serviceIntent = new Intent(this, ChatService.class);
        serviceIntent.putExtra("FromLogin",false);
        bindService(serviceIntent, mConnection, this.BIND_AUTO_CREATE);


//        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1234);
//        }


//        db = new MyDatabaseOpenHelper(this,"catchMind",null,1);
//        Cursor cursor = db.getChatFriendList();
//        Log.d("getfriend메시CFL",cursor.getColumnName(0)+"####"+cursor.getColumnName(1)+"####"+cursor.getColumnName(2)+"####"+cursor.getColumnName(3)+"####"+cursor.getColumnName(4));
//        while(cursor.moveToNext()) {
//            Log.d("getfriendCFL", cursor.getString(0) + "###" + cursor.getString(1));
//        }
//        cursor = db.getChatRoomList();
//        Log.d("getfriend메시CRL",cursor.getColumnName(0)+"####"+cursor.getColumnName(1)+"####"+cursor.getColumnName(2));
//        while(cursor.moveToNext()){
//        Log.d("getfriend메시CRL",cursor.getString(0)+"###"+cursor.getString(1));
//        }


//        ArrayList<String> test = new ArrayList<>();
//        test.add("zero");
//        test.add("one");
//
//        test.add(1,"cute");
//
//        Log.d("아리0",test.get(0));
//        Log.d("아리1",test.get(1));
//        Log.d("아리2",test.get(2));



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


    @Override
    public void sendToActivity(String friendId,String nickname) {
        viewPager.setCurrentItem(1);
        fragmentCommunicator.startChatRoomActivity(0,friendId,nickname);
    }



    private ServiceConnection mConnection = new ServiceConnection() {
        // Called when the connection with the service is established
        public void onServiceConnected(ComponentName className, IBinder service) {
            ChatService.ChatServiceBinder binder = (ChatService.ChatServiceBinder) service;
            mService = binder.getService(); //서비스 받아옴
            mService.registerCallback_2(mCallback); //콜백 등록
            mService.boundCheck_2 = true;
        }

        // Called when the connection with the service disconnects unexpectedly
        public void onServiceDisconnected(ComponentName className) {
            mService = null;
        }
    };

    private ChatService.ICallback_2 mCallback = new ChatService.ICallback_2() {

        public void recvData() {

            Message message= Message.obtain();
            message.what = 1;
            handler.sendMessage(message);

        }

        public void changeRoomList(){

            Message message= Message.obtain();
            message.what = 2;
            handler.sendMessage(message);

        }

    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mService.boundCheck_2 = false;
        unbindService(mConnection);


        boolean autoLogin = mPref.getBoolean("autoLogin",false);
        Log.d("MainActivity","onDestroy"+autoLogin);
        if(!autoLogin){
            mService.terminateService();
        }

        unregisterReceiver(mNCR);
        unregisterReceiver(NetworkChangeUpdater);
        mNCR = null;

    }

    public interface FragmentCommunicator {

        void notifyRecvData();
        void changeRoomListFC();
        void startChatRoomActivity(int no,String friendId, String nickname);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if(tabPosition == 0) {
            getMenuInflater().inflate(R.menu.friend_menu, menu);
        }else if(tabPosition ==1){
            getMenuInflater().inflate(R.menu.chatroom_menu, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //ActionBar 메뉴 클릭에 대한 이벤트 처리
        String txt = null;
        int id = item.getItemId();
        switch (id){
            case R.id.action_align:

                break;
            case R.id.action_search_friend:

                Intent intentSF = new Intent(this,SearchFriendActivity.class);
                startActivity(intentSF);
                break;

            case R.id.action_search_chatroom:

                Intent intentSR = new Intent(this,SearchRoomActivity.class);
                startActivity(intentSR);
                break;


            case R.id.add_friend:
                Intent intentadd = new Intent(this,AddFriendActivity.class);
                startActivity(intentadd);
                break;
            case R.id.edit_friend:

                Intent intent = new Intent(this,EditFriendActivity.class);
                startActivity(intent);
                break;

            case R.id.add_chatroom:

                Intent intentMakeGroup = new Intent(this,MakeGroupActivity.class);
                intentMakeGroup.putExtra("FCR",false);
                startActivityForResult(intentMakeGroup,MakeGroupActivity);
                break;

            case R.id.edit_chatroom:

                Intent intentEdit = new Intent(this,EditChatRoomActivity.class);
                startActivityForResult(intentEdit,EditChatRoom);

                break;


        }


        return super.onOptionsItemSelected(item);
    }


    public void Logout(View v){

        editor.putBoolean("autoLogin",false);
        editor.commit();
        mService.terminateService();
        Intent stopIntent = new Intent(this,ChatService.class);
        stopService(stopIntent);
        Log.d("MainActivity","Logout");

        Intent intent = new Intent(this,LoginActivity.class);
        startActivity(intent);
        finish();

    }

    public void myProfileBtn(View v){
        Intent intent = new Intent(this,ProfileActivity.class);
        intent.putExtra("position",1);
        intent.putExtra("userId",mPref.getString("userId","noUserId"));
        intent.putExtra("nickname",mPref.getString("nickname","noNickname"));
        intent.putExtra("profile",mPref.getString("profile", "noprofile"));
        intent.putExtra("message",mPref.getString("message", "Nomessage"));
        startActivity(intent);

    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == MakeGroupActivity){
            if(resultCode == RESULT_OK){
                int no = data.getExtras().getInt("no");
                String fId = data.getExtras().getString("inviteId");
                Log.d("Main.onactresult",fId);
                String nick = data.getExtras().getString("nickname");
                fragmentCommunicator.startChatRoomActivity(no,fId,nick);
            }
        }else if(requestCode == EditChatRoom){
            if(resultCode == RESULT_OK){
                try {
                    String roomSet = data.getExtras().getString("roomSet");

                    JSONArray jarray = new JSONArray(roomSet);

                    String content =  nickname + "님이 나갔습니다";
                    long now = System.currentTimeMillis();

                    for(int i=0;i<jarray.length();i++){

                        JSONObject jsonObject = new JSONObject(jarray.get(i).toString());

                        if(jsonObject.getBoolean("group")){

                            int no = jsonObject.getInt("id");

                            String friendIdExit = jsonObject.getString("FIE");

                            ExitThread et = new ExitThread(no,friendIdExit);
                            et.start();



                        }else{

                            mService.sendExit(0,jsonObject.getString("id"),content,now);
                        }

                    }


                }catch (JSONException e){
                    e.printStackTrace();
                }
            }

        }
    }




    public class ExitThread extends Thread{


        int no;
        String content;
        long now;
        String friendIdExit;

        public ExitThread(int No,String FriendIdExit){
            this.no = No;
            this.now = System.currentTimeMillis();
            this.content = nickname + "님이 나갔습니다";
            this.friendIdExit = FriendIdExit;
        }

        @Override
        public void run() {

            mService.sendExit(no,friendIdExit,content,now);
        }


    }



}
