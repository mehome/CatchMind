package com.catchmind.catchmind;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
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

public class MainActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    Toolbar toolbar;
    public MyDatabaseOpenHelper db;
    public SharedPreferences mPref;
    public SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Adding Toolbar to the activity
       toolbar = (Toolbar) findViewById(R.id.toolbar);
       setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true); //커스터마이징 하기 위해 필요
//        actionBar.setDisplayShowTitleEnabled(false);
        getSupportActionBar().setTitle("메신저");

        mPref = getSharedPreferences("login",MODE_PRIVATE);
        editor = mPref.edit();
        Bundle bundle = new Bundle();
        bundle.putString("nickname",mPref.getString("nickname","닉없음"));
        bundle.putString("message",mPref.getString("message","메세지없음"));

        // Initializing the TabLayout
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.profile_icon_act));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.chat_icon_inact));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.setting_icon_inact));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        // Initializing ViewPager
        viewPager = (ViewPager) findViewById(R.id.pager);

        // Creating TabPagerAdapter adapter
        TabPagerAdapter pagerAdapter = new TabPagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount(),mPref);
        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        // Set TabSelectedListener
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();

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


//        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1234);
//        }

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
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

        }


        return super.onOptionsItemSelected(item);
    }


    public void Logout(View v){

        editor.putBoolean("autoLogin",false);
        editor.commit();

        Intent intent = new Intent(this,LoginActivity.class);
        startActivity(intent);
        finish();
    }

}
