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
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;


public class EditFriendActivity extends AppCompatActivity {

    public MyDatabaseOpenHelper db;
    ListView friendList;
    FriendListViewAdapter friendListAdapter;
    Toolbar toolbar;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_friend);

        toolbar = (Toolbar) findViewById(R.id.toolbarEditFriend);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("편집");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        final ArrayList<ListViewItem> ListData = new ArrayList<ListViewItem>();
        ArrayList<ListViewItem> FListData = new ArrayList<ListViewItem>();
        ArrayList<String> BookmarkList = new ArrayList<String>();


        db = new MyDatabaseOpenHelper(this,"catchMind",null,1);
        Cursor cursor = db.getList();

        while(cursor.moveToNext()) {

            ListViewItem addItem = new ListViewItem(cursor.getString(0),cursor.getString(1),cursor.getString(2),cursor.getString(3));
            ListData.add(addItem);
            if(cursor.getInt(4) == 1){
                FListData.add(addItem);
                BookmarkList.add(addItem.getId());
            }

            Log.d("EditFriendActivity", cursor.getString(0)+"#####"+cursor.getString(1) + "" +cursor.getString(2));
        }


        friendList = (ListView) findViewById(R.id.editFriendList);

        friendListAdapter = (new FriendListViewAdapter(this,FListData,ListData,BookmarkList));

        friendList.setAdapter(friendListAdapter);


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
