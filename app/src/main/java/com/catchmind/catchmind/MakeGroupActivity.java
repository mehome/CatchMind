package com.catchmind.catchmind;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by sonsch94 on 2017-08-08.
 */

public class MakeGroupActivity extends AppCompatActivity {
    public MyDatabaseOpenHelper db;
    ListView friendList;
    InviteListViewAdapter friendListAdapter;
    Toolbar toolbar;
    TextView groupNumTV;
    int groupNum;
    ArrayList<String> inviteList = new ArrayList<>();
    String myId;
    public SharedPreferences mPref;
    public SharedPreferences.Editor editor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_group);

        toolbar = (Toolbar) findViewById(R.id.toolbarInviteFriend);
        groupNumTV = (TextView) toolbar.findViewById(R.id.groupNum);
        groupNum = 0;
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("대화상대초대");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mPref = getSharedPreferences("login",MODE_PRIVATE);
        myId = mPref.getString("userId","닉없음");
        editor = mPref.edit();


        final ArrayList<ListViewItemCheck> ListData = new ArrayList<ListViewItemCheck>();
        ArrayList<ListViewItemCheck> FListData = new ArrayList<ListViewItemCheck>();

        HashMap<String,Boolean> isChecked = new HashMap<>();

        db = new MyDatabaseOpenHelper(this,"catchMind",null,1);
        Cursor cursor = db.getList();

        while(cursor.moveToNext()) {

            ListViewItemCheck addItem = new ListViewItemCheck(cursor.getString(0),cursor.getString(1),cursor.getString(2));
            ListData.add(addItem);
            if(cursor.getInt(4) == 1){
                FListData.add(addItem);
            }
            isChecked.put(cursor.getString(0),false);

            Log.d("MakeGroupActivity", cursor.getString(0)+"#####"+cursor.getString(1) + "###" +cursor.getString(2));
        }


        friendList = (ListView) findViewById(R.id.inviteFriendList);

        friendListAdapter = (new InviteListViewAdapter(this,FListData,ListData,isChecked));

        friendList.setAdapter(friendListAdapter);

        friendList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String userId = (String)view.getTag(R.id.userId);
//                ImageView IV = (ImageView)view.getTag(R.id.checkIcon);
//                if(friendListAdapter.isChecked.get(userId)) {
//                    IV.setImageResource(R.drawable.check_icon_inact);
//                }else{
//                    IV.setImageResource(R.drawable.check_icon);
//                }

                if(friendListAdapter.isChecked.get(userId)) {
                    groupNum = groupNum -1;
                    groupNumTV.setText(groupNum+"");
                    inviteList.remove(userId);
                }else{
                    groupNum = groupNum +1;
                    groupNumTV.setText(groupNum+"");
                    inviteList.add(userId);
                }

                friendListAdapter.changeIsChecked(userId);
                friendListAdapter.notifyDataSetChanged();

            }
        });

    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here

        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }else if(id == R.id.invite_check_button){

            JSONArray jsonArray = new JSONArray();
            for (int i=0; i < inviteList.size(); i++) {
                jsonArray.put(inviteList.get(i));
            }

            Intent intent = new Intent();
            intent.putExtra("friendId",jsonArray.toString());
            intent.putExtra("nickname","임시 방제");
            setResult(RESULT_OK, intent);
            finish();

//            Toast.makeText(this,jsonArray.toString(),Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        getMenuInflater().inflate(R.menu.make_group_menu, menu);
        return true;
    }

}
