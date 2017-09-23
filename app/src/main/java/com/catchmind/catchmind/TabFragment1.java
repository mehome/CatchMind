package com.catchmind.catchmind;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v4.content.SharedPreferencesCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;


public class TabFragment1 extends Fragment {

    public Cursor cursor;
    public MyDatabaseOpenHelper db;
    public SharedPreferences mPref;
    public SharedPreferences.Editor editor;
    public ListViewAdapter myListAdapter;
    public static final int sendVideoCall = 235711;
    ListView lv;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.tab_fragment_1, container, false);

        ArrayList<ListViewItem> ListData = new ArrayList<ListViewItem>();
        ArrayList<ListViewItem> FListData = new ArrayList<ListViewItem>();


        String myId = getArguments().getString("userId");
        String myNickname = getArguments().getString("nickname");
        String myProfile = getArguments().getString("profile");
        String myMessage = getArguments().getString("message");

        mPref = getActivity().getSharedPreferences("login",getActivity().MODE_PRIVATE);

        ListViewItem myItem = new ListViewItem(myId,myNickname,myProfile,myMessage);

        db = new MyDatabaseOpenHelper(getContext(),"catchMind",null,1);
        Cursor cursor = db.getList();

        while(cursor.moveToNext()) {

            ListViewItem addItem = new ListViewItem(cursor.getString(0),cursor.getString(1),cursor.getString(2),cursor.getString(3));
            ListData.add(addItem);
            if(cursor.getInt(4) == 1){
                FListData.add(addItem);
            }

            Log.d("커서야", cursor.getString(0)+"#####"+cursor.getString(1) + "" +cursor.getString(2));
        }


//        Cursor cs = db.getMessageListJoinFriendList(myId,"thdwndrl");
//
//        while(cs.moveToNext()) {
//
//            Log.d("죽을때", cs.getString(0)+"#####"+cs.getString(1) + "#####" +cs.getString(2)+"#####"+cs.getString(3)+"#####"+cs.getString(4)+"#####"+cs.getString(5)+"#####"+cs.getString(6)+"#####"+cs.getString(7)+"#####"+cs.getString(8));
//        }


        lv = (ListView) rootView.findViewById(R.id.list);

        myListAdapter = (new ListViewAdapter(getActivity().getApplicationContext(),myItem,FListData,ListData));

        lv.setAdapter(myListAdapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String userId =(String) view.getTag(R.id.userId);
                String nickname =(String) view.getTag(R.id.nickname);
                String profile =(String) view.getTag(R.id.profile);
                String message = (String) view.getTag(R.id.message);

                if(userId.equals("")){
                    return;
                }

//                Toast.makeText(getActivity().getApplicationContext(),""+position+"###"+userId,Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity().getApplicationContext(),ProfileActivity.class);
                intent.putExtra("position",position);
                intent.putExtra("userId",userId);
                intent.putExtra("nickname",nickname);
                intent.putExtra("profile",profile);
                intent.putExtra("message",message);
                startActivityForResult(intent,1234);

            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        String myId = getArguments().getString("userId");
        String myNickname = mPref.getString("nickname","노닉네임");
        String myProfile = getArguments().getString("profile");
        String myMessage = mPref.getString("message","노메세지");


        ListViewItem myItem = new ListViewItem(myId,myNickname,myProfile,myMessage);

        myListAdapter.changeMyItem(myItem);

        ArrayList<ListViewItem> ListData = new ArrayList<ListViewItem>();
        ArrayList<ListViewItem> FListData = new ArrayList<ListViewItem>();

        db = new MyDatabaseOpenHelper(getContext(),"catchMind",null,1);
        Cursor cursor = db.getList();

        while(cursor.moveToNext()) {

            ListViewItem addItem = new ListViewItem(cursor.getString(0),cursor.getString(1),cursor.getString(2),cursor.getString(3));
            ListData.add(addItem);
            if(cursor.getInt(4) == 1){
                FListData.add(addItem);
            }

            Log.d("리쥼", cursor.getString(0)+"#####"+cursor.getString(1) + "" +cursor.getString(2));
        }

        myListAdapter.ChangeList(FListData,ListData);
        myListAdapter.sizeReset();
        myListAdapter.notifyDataSetChanged();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);

        Log.d("태프원",resultCode+"");

        if(resultCode == RESULT_OK) {
//            Toast.makeText(getContext(), requestCode + "###" + resultCode, Toast.LENGTH_SHORT).show();
            String friendId = data.getExtras().getString("friendId");
            String nickname = data.getExtras().getString("nickname");
            STA.sendToActivity(friendId,nickname);
        }

//        if(resultCode == sendVideoCall){
//            String friendId = data.getExtras().getString("friendId");
//            String roomId = data.getExtras().getString("roomId");
//            Toast.makeText(getContext(),friendId,Toast.LENGTH_SHORT).show();
//
//
//        }
    }



    public interface sendToActivity {
        void sendToActivity(String friendId,String nickname);
    }

    sendToActivity STA;



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        MainActivity MA = (MainActivity) context;
        try {
            STA = (sendToActivity) MA;
        } catch (ClassCastException e) {
            throw new ClassCastException(MA.toString() + " must implement onSomeEventListener");
        }
    }
}
