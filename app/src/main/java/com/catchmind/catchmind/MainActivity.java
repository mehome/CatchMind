package com.catchmind.catchmind;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
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

import java.text.SimpleDateFormat;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tabPosition = 0;

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

        editor = mPref.edit();
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


        Intent serviceIntent = new Intent(this, ChatService.class);
        bindService(serviceIntent, mConnection, this.BIND_AUTO_CREATE);


//        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1234);
//        }


        db = new MyDatabaseOpenHelper(this,"catchMind",null,1);
        Cursor cursor = db.getChatFriendList(userId);
        Log.d("getfriend메시CFL",cursor.getColumnName(0)+"####"+cursor.getColumnName(1)+"####"+cursor.getColumnName(2)+"####"+cursor.getColumnName(3)+"####"+cursor.getColumnName(4)+"####"+cursor.getColumnName(5));
        while(cursor.moveToNext()) {
            Log.d("getfriendCFL", cursor.getString(0) + "###" + cursor.getString(1));
        }
        cursor = db.getChatRoomList(userId);
        Log.d("getfriend메시CRL",cursor.getColumnName(0)+"####"+cursor.getColumnName(1)+"####"+cursor.getColumnName(2));
        while(cursor.moveToNext()){
        Log.d("getfriend메시CRL",cursor.getString(0)+"###"+cursor.getString(1));
        }
    }

    @Override
    public void sendToActivity(String friendId,String nickname) {
        viewPager.setCurrentItem(1);
        fragmentCommunicator.startChatRoomActivity(friendId,nickname);
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

    }

    public interface FragmentCommunicator {

        void notifyRecvData();
        void changeRoomListFC();
        void startChatRoomActivity(String friendId, String nickname);

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
            case R.id.action_search:
                txt = "search click";
                Toast.makeText(this,txt, Toast.LENGTH_LONG).show();
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
                startActivity(intentMakeGroup);
                break;

        }


        return super.onOptionsItemSelected(item);
    }


    public void Logout(View v){

        editor.putBoolean("autoLogin",false);
        editor.commit();
        mService.terminateService();
        Log.d("MainActivity","Logout");

        Intent intent = new Intent(this,LoginActivity.class);
        startActivity(intent);
        finish();

    }


}
