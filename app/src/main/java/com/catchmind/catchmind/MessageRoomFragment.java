package com.catchmind.catchmind;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by sonsch94 on 2017-07-19.
 */

public class MessageRoomFragment extends Fragment implements ChatRoomActivity.FragmentCommunicator{

    ChatMessageAdapter chatListAdapter;
    ArrayList<ChatMessageItem> ListData;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.message_room_fragment, container, false);

        ChatRoomActivity activity = (ChatRoomActivity) getActivity();
        String Nickname = activity.getNickname();

        ListData = new ArrayList<ChatMessageItem>();

        ChatMessageItem[] addItem = new ChatMessageItem[20];

        addItem[0] = new ChatMessageItem(0,"무쓸모닉", "2017년 7월 20일 목요일","무쓸모타임");
        ListData.add(addItem[0]);

//        for(int i=1;i<20;i++){
//            if(i%2 == 1) {
//                addItem[i] = new ChatMessageItem(1,Nickname,"내용내용내용내용내용내용내용내용내용내용내용내용내용내용내용내용"+i,"오후 5시 2"+i+"분" );
//            }else{
//                addItem[i] = new ChatMessageItem(2,"무쓸모닉","내용내용내용내용내용내용내용내용내용내용내용내용내용내용내용내용"+i,"오후 5시 2"+i+"분" );
//            }
//
//            ListData.add(addItem[i]);
//        }

        ListView lv = (ListView) rootView.findViewById(R.id.messageList);

        chatListAdapter = (new ChatMessageAdapter(getActivity().getApplicationContext(),ListData));

        lv.setAdapter(chatListAdapter);



        return rootView;
    }

    @Override
    public void passData(String name,String content,int type) {
//        Toast.makeText(getActivity(),passdata+" 프래그먼트",Toast.LENGTH_SHORT).show();

        if(type == 1) {
            ChatMessageItem addItem = new ChatMessageItem(1, name, content, "오후 5시 21분");
            ListData.add(addItem);
            chatListAdapter.notifyDataSetChanged();
        }else{
            ChatMessageItem addItem = new ChatMessageItem(2, name, content, "오후 5시 21분");
            ListData.add(addItem);
            chatListAdapter.notifyDataSetChanged();
        }
    }
}
