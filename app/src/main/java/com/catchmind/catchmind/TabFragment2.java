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
//        Log.d("진짜로?",myId);
        myNickname = getArguments().getString("nickname");


        ArrayList<ChatRoomItem> ListData = new ArrayList<ChatRoomItem>();


        db = new MyDatabaseOpenHelper(getContext(),"catchMind",null,1);
//        Cursor cursor = db.getChatRoomList();
//        Cursor cursor = db.getChatRoomListUnread();
          Cursor cursor = db.getChatRoomListJoinWithMessage();

//        SimpleDateFormat sdfNow = new SimpleDateFormat("HH:mm");
//        Date recvTime = new Date(cursor.getLong(2));
//        String time = sdfNow.format(recvTime);

        while(cursor.moveToNext()) {

            ChatRoomItem addItem = new ChatRoomItem(cursor.getInt(0),cursor.getString(1),cursor.getLong(7));
            if(ListData.size() == 0) {
                ListData.add(addItem);
            }else{

                int addPosition = 0;
                for(int i = 0 ; i < ListData.size() ; i++){
                    if(addItem.recentMessageTime >= ListData.get(i).recentMessageTime){
                        addPosition = i;
                        break;
                    }
                }
                ListData.add(addPosition,addItem);
            }

            Log.d("커서야ChatRoomItem",cursor.getString(0)+"#####"+cursor.getString(1));
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


        //test

        Cursor tc = db.getChatRoomListJoinWithMessage();

//        Log.d("별이",tc.getColumnName(0)+"####"+tc.getColumnName(1)+"####"+tc.getColumnName(2)+"####"+tc.getColumnName(3));
        int i=0;
        while(tc.moveToNext()){

            Log.d("별이"+i,tc.getInt(0)+"###"+tc.getString(1)+"###"+tc.getLong(2)+"###"+tc.getInt(3)+"###"+tc.getInt(4)+"###"+tc.getString(5)+"###"+tc.getString(6)+"####"+tc.getLong(7));
            i++;
        }


        return rootView;
    }


    @Override
    public void notifyRecvData(){
        if(myListAdapter != null) {
            myListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void changeRoomListFC() {
//        Log.d("진짜로?",myId);
//        Cursor cursor = db.getChatRoomList();
//        Cursor cursor = db.getChatRoomListUnread();
        if(db ==null){
            return;
        }
        Cursor cursor = db.getChatRoomListJoinWithMessage();
        ArrayList<ChatRoomItem> ListData = new ArrayList<>();

        while(cursor.moveToNext()) {

            ChatRoomItem addItem = new ChatRoomItem(cursor.getInt(0),cursor.getString(1),cursor.getLong(7));
            if(ListData.size() == 0) {
                ListData.add(addItem);
            }else{

                int addPosition = -1;
                for(int i = 0 ; i < ListData.size() ; i++){
                    if(addItem.recentMessageTime >= ListData.get(i).recentMessageTime){
                        Log.d("산이",addItem.recentMessageTime+"###"+ListData.get(i).recentMessageTime+"###"+i+"###"+ListData.get(i).getFriendId());
                        addPosition = i;
                        break;
                    }
                }
                Log.d("아리",addPosition+"###"+ListData.get(0).getFriendId());
                if(addPosition != -1) {
                    ListData.add(addPosition, addItem);
                }else{
                    ListData.add(addItem);
                }
            }

            for(int i = 0; i < ListData.size();i++) {
                Log.d("달이"+i,ListData.get(i).getFriendId() );
            }

            Log.d("커서야ChatRoomItem",cursor.getString(0)+"#####"+cursor.getString(1));
        }

        myListAdapter.ChangeList(ListData);
        myListAdapter.notifyDataSetChanged();

    }

    @Override
    public void startChatRoomActivity(int no,String friendId,String nickname) {

        Intent intent = new Intent(getActivity().getApplicationContext(), ChatRoomActivity.class);
        intent.putExtra("friendId",friendId);
        Log.d("scra",friendId);
        intent.putExtra("nickname",nickname);
        intent.putExtra("no",no);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        changeRoomListFC();
//        myListAdapter.notifyDataSetChanged();
    }
}
