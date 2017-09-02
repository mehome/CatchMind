package com.catchmind.catchmind;

/**
 * Created by sonsch94 on 2017-09-02.
 */

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by sonsch94 on 2017-07-19.
 */

public class drawChatAdapter extends BaseAdapter {


    public ArrayList<drawChatItem> drawChatList = new ArrayList<drawChatItem>() ;
    public Context mContext;
    public LayoutInflater inflater ;
    public MyDatabaseOpenHelper db;
    public String myId;
    public String zeroFriendId;
    public int no;
    public SimpleDateFormat sdfNow ;
    public SimpleDateFormat sdfDate ;
    // ListViewAdapter의 생성자
    public drawChatAdapter(Context context,String myId) {
        this.mContext = context;
        this.inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.drawChatList = new ArrayList<>();
        this.myId = myId;
    }


    // Adapter에 사용되는 데이터의 개수를 리턴. : 필수 구현
    @Override
    public int getCount() {
        return drawChatList.size();
    }


    // position에 위치한 데이터를 화면에 출력하는데 사용될 View를 리턴. : 필수 구현
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        drawChatViewholder viewHolder;
        String friendId = "";
        String nickname = "";
        String profile = "";


        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
        if (convertView == null) {
            convertView = this.inflater.inflate(R.layout.draw_chat_item, parent, false);

            viewHolder = new drawChatViewholder();
            viewHolder.NickTV = (TextView) convertView.findViewById(R.id.drawChatNickname);
            viewHolder.ContentTV = (TextView) convertView.findViewById(R.id.drawChatContent);

            convertView.setTag(viewHolder);

        }else{
            viewHolder = (drawChatViewholder) convertView.getTag();
        }


        viewHolder.NickTV.setText(drawChatList.get(position).getNickname());
        viewHolder.ContentTV.setText(drawChatList.get(position).getContent());

        return convertView;

    }

    // 지정한 위치(position)에 있는 데이터와 관계된 아이템(row)의 ID를 리턴. : 필수 구현
    @Override
    public long getItemId(int position) {
        return position ;
    }

    // 지정한 위치(position)에 있는 데이터 리턴 : 필수 구현
    @Override
    public Object getItem(int position) {
        return drawChatList.get(position) ;
    }

    public void addItem(drawChatItem addItem){
        this.drawChatList.add(addItem);
    }
}

