package com.catchmind.catchmind;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PersistableBundle;
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

/**
 * Created by sonsch94 on 2017-09-22.
 */

public class SearchFriendActivity extends AppCompatActivity {

    ListView friendList;
    ImageView backBtn;
    ImageView searchBtn;
    EditText editText;

    SearchFriendAdapter searchFriendAdapter;

    ArrayList<SearchFriendItem> searchFriendList;
    ArrayList<SearchFriendItem> allList;

    public MyDatabaseOpenHelper db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_friend);

        backBtn = (ImageView) findViewById(R.id.search_friend_back);
        editText = (EditText) findViewById(R.id.search_friend_editText);
        searchBtn = (ImageView) findViewById(R.id.search_friend_search);

        friendList = (ListView) findViewById(R.id.list_friend_search);

        searchFriendList = new ArrayList<>();
        allList = new ArrayList<>();

        db = new MyDatabaseOpenHelper(this,"catchMind",null,1);

        Cursor cursor = db.getList();

        while(cursor.moveToNext()) {

            SearchFriendItem addItem = new SearchFriendItem(cursor.getString(0),cursor.getString(1),cursor.getString(2),cursor.getString(3));
            searchFriendList.add(addItem);
            allList.add(addItem);

        }

        searchFriendAdapter = new SearchFriendAdapter(this,searchFriendList);

        friendList.setAdapter(searchFriendAdapter);



        final TextWatcher editTextWatcher = new TextWatcher() {


            @Override
            public void afterTextChanged(Editable s) {

                searchFriendAdapter.notifyDataSetChanged();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                String editContent = editText.getText().toString();
                searchFriendAdapter.clearList();

                for(int i=0; i<allList.size();i++){

                    if(allList.get(i).getName().contains(editContent)){

                        searchFriendAdapter.addSFItem(allList.get(i));

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

        friendList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String friendId = (String) view.getTag(R.id.userId);
                String nickname = (String) view.getTag(R.id.nickname);


                Intent intent = new Intent(getApplicationContext(), ChatRoomActivity.class);
                intent.putExtra("friendId",friendId);
                intent.putExtra("nickname",nickname);
                intent.putExtra("no",0);
                startActivity(intent);

                finish();

            }
        });

    }


}
