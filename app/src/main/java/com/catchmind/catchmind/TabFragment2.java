package com.catchmind.catchmind;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;


import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class TabFragment2 extends Fragment implements MainActivity.FragmentCommunicator{


    ChatRoomAdapter myListAdapter;
    MyDatabaseOpenHelper db;
    String myId;
    String myNickname;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.tab_fragment_2, container, false);

        myId = getArguments().getString("userId");

        myNickname = getArguments().getString("nickname");


        ArrayList<ChatRoomItem> ListData = new ArrayList<ChatRoomItem>();


        db = new MyDatabaseOpenHelper(getContext(),"catchMind",null,1);
        Cursor cursor = db.getChatRoomListJoinChatFriendList(myId);
//        SimpleDateFormat sdfNow = new SimpleDateFormat("HH:mm");
//        Date recvTime = new Date(cursor.getLong(2));
//        String time = sdfNow.format(recvTime);

        while(cursor.moveToNext()) {

            ChatRoomItem addItem = new ChatRoomItem(cursor.getInt(3),cursor.getString(4),cursor.getString(5),1,cursor.getString(6));
            ListData.add(addItem);

            Log.d("커서야ChatRoomItem",cursor.getString(0)+"#####"+cursor.getString(1)+"#####"+cursor.getString(2)+"#####"+cursor.getString(3)+"#####"+cursor.getString(4)+"#####"+cursor.getString(5)+"#####"+cursor.getString(6));
        }

        ListView lv = (ListView) rootView.findViewById(R.id.list);

        myListAdapter = new ChatRoomAdapter(getActivity().getApplicationContext(),ListData,myId);

        lv.setAdapter(myListAdapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String friendId = (String) view.getTag(R.id.userId);
                String nickname = (String) view.getTag(R.id.nickname);
                int no = (int) view.getTag(R.id.no);

                Intent intent = new Intent(getActivity().getApplicationContext(), ChatRoomActivity.class);
                intent.putExtra("friendId",friendId);
                intent.putExtra("nickname",nickname);
                intent.putExtra("no",no);
                startActivity(intent);

            }
        });

        return rootView;
    }


    @Override
    public void notifyRecvData(){
        myListAdapter.notifyDataSetChanged();
    }

    @Override
    public void changeRoomListFC() {
        Log.d("진짜로?",myId);
        Cursor cursor = db.getChatRoomListJoinChatFriendList(myId);
        ArrayList<ChatRoomItem> ListData = new ArrayList<>();

        while(cursor.moveToNext()) {

            ChatRoomItem addItem = new ChatRoomItem(cursor.getInt(3),cursor.getString(4),cursor.getString(5),1,cursor.getString(6));
            ListData.add(addItem);

            Log.d("커서야ChatRoomItem","changeRoomListFC");
        }

        myListAdapter.ChangeList(ListData);
        myListAdapter.notifyDataSetChanged();

    }

    @Override
    public void startChatRoomActivity(String friendId,String nickname) {

        Intent intent = new Intent(getActivity().getApplicationContext(), ChatRoomActivity.class);
        intent.putExtra("friendId",friendId);
        intent.putExtra("nickname",nickname);
        intent.putExtra("no",0);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        myListAdapter.notifyDataSetChanged();
    }
}
