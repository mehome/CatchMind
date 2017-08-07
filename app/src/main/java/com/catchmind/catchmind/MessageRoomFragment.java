package com.catchmind.catchmind;

import android.content.Intent;
import android.content.SharedPreferences;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by sonsch94 on 2017-07-19.
 */

public class MessageRoomFragment extends Fragment implements ChatRoomActivity.FragmentCommunicator{

    ChatMessageAdapter chatListAdapter;
    ArrayList<ChatMessageItem> ListData;
    SharedPreferences mPref;
    SharedPreferences.Editor editor;

    String userId;
    String friendId;
    public MyDatabaseOpenHelper db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.message_room_fragment, container, false);

        userId = getArguments().getString("userId");
        friendId = getArguments().getString("friendId");

        ListData = new ArrayList<ChatMessageItem>();

        ChatMessageItem defaultItem = new ChatMessageItem(0,"무쓸모ID","무쓸모닉", "무쓸모프로필","2017년 7월 20일 목요일","무쓸모타임");
        ListData.add(defaultItem);

        db = new MyDatabaseOpenHelper(getContext(),"catchMind",null,1);
        Cursor cursor = db.getMessageListJoinChatFriendList(userId,friendId);
        SimpleDateFormat sdfNow = new SimpleDateFormat("HH:mm");

        while(cursor.moveToNext()) {

            Date recvTime = new Date(cursor.getLong(4));
            String time = sdfNow.format(recvTime);
            ChatMessageItem addItem = new ChatMessageItem(cursor.getInt(5),cursor.getString(7),cursor.getString(8),cursor.getString(9),cursor.getString(3),time);
            ListData.add(addItem);

            Log.d("커서야ChatMessageItem",cursor.getString(0)+"#####"+cursor.getString(1)+"#####"+cursor.getString(2)+"#####"+cursor.getString(3)+"#####"+cursor.getString(4)+"#####"+cursor.getString(5)+"#####"+cursor.getString(6)+"#####"+cursor.getString(7)+"#####"+cursor.getString(8));
        }

        ListView lv = (ListView) rootView.findViewById(R.id.messageList);

        chatListAdapter = (new ChatMessageAdapter(getActivity().getApplicationContext(),ListData));

        lv.setAdapter(chatListAdapter);

        return rootView;

    }

    @Override
    public void passData(String friendId,String nickname, String profile,String content,long now,int type) {
//        Toast.makeText(getActivity(),passdata+" 프래그먼트",Toast.LENGTH_SHORT).show();

        SimpleDateFormat sdfNow = new SimpleDateFormat("HH:mm");
        Date recvTime = new Date(now);
        String time = sdfNow.format(recvTime);

        if(type == 1) {
            ChatMessageItem addItem = new ChatMessageItem(1, friendId, nickname, profile, content, time);
            ListData.add(addItem);
            chatListAdapter.notifyDataSetChanged();
        }else{
            ChatMessageItem addItem = new ChatMessageItem(2, friendId, nickname, profile, content, time);
            ListData.add(addItem);
            chatListAdapter.notifyDataSetChanged();
        }
    }



}
