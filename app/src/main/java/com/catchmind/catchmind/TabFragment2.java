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

import java.util.ArrayList;


public class TabFragment2 extends Fragment {


    ChatRoomAdapter myListAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.tab_fragment_2, container, false);

        ArrayList<ChatRoomItem> ListData = new ArrayList<ChatRoomItem>();

        ChatRoomItem[] addItem = new ChatRoomItem[10];


        addItem[0] = new ChatRoomItem("nova","대화내용",1,"날짜");
        ListData.add(addItem[0]);
        addItem[1] = new ChatRoomItem("thdwndrl","대화내용",1,"날짜");
        ListData.add(addItem[1]);


        ListView lv = (ListView) rootView.findViewById(R.id.list);

        myListAdapter = new ChatRoomAdapter(getActivity().getApplicationContext(),ListData);

        lv.setAdapter(myListAdapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String conversation =(String) view.getTag(R.id.conversation);
//                Toast.makeText(getActivity().getApplicationContext(),""+position+"###"+conversation ,Toast.LENGTH_SHORT).show();
                ChatRoomItem CRI = (ChatRoomItem) myListAdapter.getItem(position);
                String friendId = CRI.getTitle();

                Intent intent = new Intent(getActivity().getApplicationContext(), ChatRoomActivity.class);
                intent.putExtra("friendId",friendId);
                startActivity(intent);

            }
        });

        return rootView;
    }
}
