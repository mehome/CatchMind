package com.catchmind.catchmind;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
 * Created by sonsch94 on 2017-09-23.
 */

public class EditChatRoomActivity extends AppCompatActivity {

    public ListView editRoomList;
    public EditChatRoomAdapter editChatRoomAdapter;
    public ArrayList<ChatRoomItem> chatRoomList;
    public HashMap<String,Boolean> IsChecked;
    public ImageView allCheck;

    MyDatabaseOpenHelper db;

    public SharedPreferences mPref;
    public SharedPreferences.Editor editor;

    public String myId;

    public Toolbar toolbar;

    public boolean allChecked;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_chatroom);

        toolbar = (Toolbar) findViewById(R.id.toolbarEditRoom);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("편집");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        editRoomList = (ListView) findViewById(R.id.editRoomList);
        allCheck = (ImageView) findViewById(R.id.all_check_IV);
        db = new MyDatabaseOpenHelper(this,"catchMind",null,1);
        chatRoomList = new ArrayList<>();
        IsChecked = new HashMap<>();

        allChecked = false;

        mPref = getSharedPreferences("login",MODE_PRIVATE);
        myId = mPref.getString("userId","닉없음");

        Cursor cursor = db.getChatRoomListJoinWithMessage();

        while(cursor.moveToNext()) {

            ChatRoomItem addItem = new ChatRoomItem(cursor.getInt(0),cursor.getString(1),cursor.getLong(7));

            if(cursor.getInt(0) == 0){
                IsChecked.put(cursor.getString(1),false);
            }else{
                IsChecked.put(cursor.getInt(0)+"",false);
            }

            if(chatRoomList.size() == 0) {

                chatRoomList.add(addItem);

            }else{

                int addPosition = 0;
                for(int i = 0 ; i < chatRoomList.size() ; i++){
                    if(addItem.recentMessageTime >= chatRoomList.get(i).recentMessageTime){
                        addPosition = i;
                        break;
                    }
                }

                chatRoomList.add(addPosition,addItem);

            }

        }


        editChatRoomAdapter = new EditChatRoomAdapter(this,chatRoomList,myId,IsChecked);

        editRoomList.setAdapter(editChatRoomAdapter);

        editRoomList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String userId = (String)view.getTag(R.id.userId);
                int no = (int)view.getTag(R.id.no);
                if(no==0) {
                    editChatRoomAdapter.changeIsChecked(userId);
                }else{
                    editChatRoomAdapter.changeIsChecked(no+"");
                }

                editChatRoomAdapter.notifyDataSetChanged();

            }
        });

        allCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(allChecked){
                    allCheck.setImageResource(R.drawable.check_icon_inact);
                    allChecked = false;
                    editChatRoomAdapter.changeAll(false);
                }else{
                    allCheck.setImageResource(R.drawable.check_icon);
                    allChecked = true;
                    editChatRoomAdapter.changeAll(true);
                }
                editChatRoomAdapter.notifyDataSetChanged();
            }
        });

    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here

        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }else if(id == R.id.exit_room_btn){

            Intent exitIntent = new Intent();

            String roomSet = editChatRoomAdapter.exitCheckedRoom();

            exitIntent.putExtra("roomSet",roomSet);

            setResult(RESULT_OK,exitIntent);

            finish();
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.

        getMenuInflater().inflate(R.menu.edit_room_menu, menu);
        return true;

    }



}
