package com.catchmind.catchmind;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by sonsch94 on 2017-07-19.
 */

public class ChatMessageAdapter extends BaseAdapter {


    public ArrayList<ChatMessageItem> chatMessageList = new ArrayList<ChatMessageItem>() ;
    public Context mContext;
    public LayoutInflater inflater ;

    // ListViewAdapter의 생성자
    public ChatMessageAdapter(Context context,ArrayList<ChatMessageItem> ListData ) {
        this.mContext = context;
        this.inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.chatMessageList = ListData;
    }

    public void setChatRoomList(ArrayList<ChatMessageItem> ListData) {
        this.chatMessageList = ListData;
    }

    // Adapter에 사용되는 데이터의 개수를 리턴. : 필수 구현
    @Override
    public int getCount() {
        return chatMessageList.size();
    }


    // position에 위치한 데이터를 화면에 출력하는데 사용될 View를 리턴. : 필수 구현
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        MessageViewHolder viewHolder;
        String userId = "";
        String nickname = "";
        String profile = "";

        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
        if (convertView == null) {
            convertView = this.inflater.inflate(R.layout.chatmessage_item, parent, false);

            viewHolder = new MessageViewHolder();
            viewHolder.layout = (LinearLayout) convertView.findViewById(R.id.layout);
            viewHolder.profileImage = (ImageView) convertView.findViewById(R.id.messageProfileImage);
            viewHolder.nickName = (TextView) convertView.findViewById(R.id.messageNickname);

            viewHolder.leftText = (TextView) convertView.findViewById(R.id.leftText);
            viewHolder.chatContent = (TextView) convertView.findViewById(R.id.chatContent);
            viewHolder.rightText = (TextView) convertView.findViewById(R.id.rightText);


            convertView.setTag(viewHolder);

        }else{
            viewHolder = (MessageViewHolder) convertView.getTag();
        }

        if(chatMessageList.get(position).Type == 0) {

            viewHolder.layout.setGravity(Gravity.CENTER);
            String dateline = "------------------";
            dateline = dateline + chatMessageList.get(position).getContent() + "------------------";
            viewHolder.chatContent.setText(dateline);
            viewHolder.chatContent.setBackgroundResource(0);
            viewHolder.profileImage.setVisibility(View.GONE);
            viewHolder.nickName.setVisibility(View.GONE);
            viewHolder.chatContent.setVisibility(View.VISIBLE);
            viewHolder.leftText.setVisibility(View.GONE);
            viewHolder.rightText.setVisibility(View.GONE);


        }else if(chatMessageList.get(position).Type == 1){

            viewHolder.layout.setGravity(Gravity.LEFT);
            viewHolder.chatContent.setText(chatMessageList.get(position).getContent());
            viewHolder.chatContent.setBackgroundResource(R.drawable.inchat);
            viewHolder.nickName.setGravity(Gravity.LEFT);
            viewHolder.nickName.setText(chatMessageList.get(position).getNickname());
            viewHolder.rightText.setText(chatMessageList.get(position).getTime());
            viewHolder.profileImage.setVisibility(View.VISIBLE);
            viewHolder.nickName.setVisibility(View.VISIBLE);
            viewHolder.chatContent.setVisibility(View.VISIBLE);
            viewHolder.leftText.setVisibility(View.GONE);
            viewHolder.rightText.setVisibility(View.VISIBLE);


        }else if(chatMessageList.get(position).Type == 2){

            viewHolder.layout.setGravity(Gravity.RIGHT);
            viewHolder.chatContent.setText(chatMessageList.get(position).getContent());
            viewHolder.chatContent.setBackgroundResource(R.drawable.outchat);
            viewHolder.nickName.setGravity(Gravity.RIGHT);
            viewHolder.leftText.setGravity(Gravity.RIGHT);
            viewHolder.leftText.setText(chatMessageList.get(position).getTime());
            viewHolder.profileImage.setVisibility(View.GONE);
            viewHolder.nickName.setVisibility(View.GONE);
            viewHolder.chatContent.setVisibility(View.VISIBLE);
            viewHolder.leftText.setVisibility(View.VISIBLE);
            viewHolder.rightText.setVisibility(View.GONE);


        }

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
        return chatMessageList.get(position) ;
    }

    // 아이템 데이터 추가를 위한 함수. 개발자가 원하는대로 작성 가능.
//    public void addItem(int type, String nickname, String content, String time) {
//        ChatMessageItem item = new ChatMessageItem(type, nickname, content, time);
//
//        chatMessageList.add(item);
//    }

}
