package com.catchmind.catchmind;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

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
                }else{
                    groupNum = groupNum +1;
                    groupNumTV.setText(groupNum+"");
                }

                friendListAdapter.changeIsChecked(userId);
                friendListAdapter.notifyDataSetChanged();

            }
        });

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
