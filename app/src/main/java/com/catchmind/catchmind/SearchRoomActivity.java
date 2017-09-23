package com.catchmind.catchmind;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by sonsch94 on 2017-09-22.
 */

public class SearchRoomActivity extends AppCompatActivity{


    ListView roomList;
    ImageView backBtn;
    ImageView searchBtn;
    EditText editText;

    SearchRoomAdapter searchRoomAdapter;

    ArrayList<ChatRoomItem> searchRoomList;
    ArrayList<ChatRoomItem> allList;

    HashMap<String,String> nicknameList;

    public MyDatabaseOpenHelper db;

    public SharedPreferences mPref;
    public SharedPreferences.Editor editor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_room);

        mPref = getSharedPreferences("login",MODE_PRIVATE);
        String myId = mPref.getString("userId","닉없음");

        backBtn = (ImageView) findViewById(R.id.search_room_back);
        editText = (EditText) findViewById(R.id.search_room_editText);
        searchBtn = (ImageView) findViewById(R.id.search_room_search);

        roomList = (ListView) findViewById(R.id.list_room_search);

        searchRoomList = new ArrayList<>();
        allList = new ArrayList<>();

        nicknameList = new HashMap<>();

        db = new MyDatabaseOpenHelper(this,"catchMind",null,1);

        Cursor cursor = db.getChatRoomListJoinWithMessage();

        while(cursor.moveToNext()) {

            ChatRoomItem addItem = new ChatRoomItem(cursor.getInt(0),cursor.getString(1),cursor.getLong(7));

            if(searchRoomList.size() == 0) {

                searchRoomList.add(addItem);
                allList.add(addItem);

            }else{

                int addPosition = 0;
                for(int i = 0 ; i < searchRoomList.size() ; i++){
                    if(addItem.recentMessageTime >= searchRoomList.get(i).recentMessageTime){
                        addPosition = i;
                        break;
                    }
                }
                searchRoomList.add(addPosition,addItem);
                allList.add(addItem);

            }

        }

        Cursor cursor2 = db.getChatFriendList();

        while(cursor2.moveToNext()) {
            nicknameList.put(cursor2.getString(1),cursor2.getString(2));
        }

        searchRoomAdapter = new SearchRoomAdapter(this,searchRoomList,myId);

        roomList.setAdapter(searchRoomAdapter);

        final TextWatcher editTextWatcher = new TextWatcher() {


            @Override
            public void afterTextChanged(Editable s) {

                searchRoomAdapter.notifyDataSetChanged();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                String editContent = editText.getText().toString();
                searchRoomAdapter.clearList();

                for(int i=0; i<allList.size();i++){

                    if(allList.get(i).getNo() == 0) {
                        if (nicknameList.get(allList.get(i).getFriendId()).contains(editContent)) {

                            searchRoomAdapter.addCRItem(allList.get(i));

                        }
                    }else{
                        if (allList.get(i).getFriendId().contains(editContent)) {

                            searchRoomAdapter.addCRItem(allList.get(i));

                        }
                    }

                }


            }



        };

        editText.addTextChangedListener(editTextWatcher);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        roomList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String friendId = (String) view.getTag(R.id.userId);
                String nickname = (String) view.getTag(R.id.nickname);
                int no = (int) view.getTag(R.id.no);

                Intent intent = new Intent(getApplicationContext(), ChatRoomActivity.class);
                intent.putExtra("friendId",friendId);
                intent.putExtra("nickname",nickname);
                intent.putExtra("no",no);
                startActivity(intent);

                finish();
            }
        });



    }

}
