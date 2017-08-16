package com.catchmind.catchmind;

import android.content.Context;
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

public class ChatMessageAdapter extends BaseAdapter {


    public ArrayList<ChatMessageItem> chatMessageList = new ArrayList<ChatMessageItem>() ;
    public Context mContext;
    public LayoutInflater inflater ;
    public MyDatabaseOpenHelper db;
    public String myId;
    public int no;
    public SimpleDateFormat sdfNow ;
    // ListViewAdapter의 생성자
    public ChatMessageAdapter(Context context,ArrayList<ChatMessageItem> ListData,String myId ,int no) {
        this.mContext = context;
        this.inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.chatMessageList = ListData;
        this.myId = myId;
        this.no = no;
        this.sdfNow = new SimpleDateFormat("HH:mm");
        db = new MyDatabaseOpenHelper(mContext,"catchMind",null,1);
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
        String friendId = "";
        String nickname = "";
        String profile = "";


        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
        if (convertView == null) {
            convertView = this.inflater.inflate(R.layout.chatmessage_item, parent, false);

            viewHolder = new MessageViewHolder();
            viewHolder.layout = (LinearLayout) convertView.findViewById(R.id.layout);
            viewHolder.leftLayout = (LinearLayout) convertView.findViewById(R.id.leftTextContainer);
            viewHolder.rightLayout = (LinearLayout) convertView.findViewById(R.id.rightTextContainer);
            viewHolder.profileImage = (ImageView) convertView.findViewById(R.id.messageProfileImage);
            viewHolder.nickName = (TextView) convertView.findViewById(R.id.messageNickname);
            viewHolder.leftText = (TextView) convertView.findViewById(R.id.leftText);
            viewHolder.leftUnread = (TextView) convertView.findViewById(R.id.leftUnread);
            viewHolder.chatContent = (TextView) convertView.findViewById(R.id.chatContent);
            viewHolder.rightText = (TextView) convertView.findViewById(R.id.rightText);
            viewHolder.rightUnread = (TextView) convertView.findViewById(R.id.rightUnread);



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
            viewHolder.leftLayout.setVisibility(View.GONE);
            viewHolder.rightLayout.setVisibility(View.GONE);



        }else if(chatMessageList.get(position).Type == 1){
            long now = chatMessageList.get(position).getTime();
            Date when = new Date(now);
            String time = sdfNow.format(when);

            viewHolder.layout.setGravity(Gravity.LEFT);
            viewHolder.chatContent.setText(chatMessageList.get(position).getContent());
            viewHolder.chatContent.setBackgroundResource(R.drawable.inchat);
            viewHolder.nickName.setGravity(Gravity.LEFT);
            viewHolder.nickName.setText(chatMessageList.get(position).getNickname());
            viewHolder.rightText.setText(time);
            viewHolder.profileImage.setVisibility(View.VISIBLE);
            viewHolder.nickName.setVisibility(View.VISIBLE);
            viewHolder.chatContent.setVisibility(View.VISIBLE);
            viewHolder.leftLayout.setVisibility(View.GONE);
            viewHolder.rightLayout.setVisibility(View.VISIBLE);
            friendId = chatMessageList.get(position).getUserId();
            profile = chatMessageList.get(position).getProfile();
            Glide.with(mContext).load("http://vnschat.vps.phps.kr/profile_image/"+friendId+".png")
                    .error(R.drawable.default_profile_image)
                    .signature(new StringSignature(profile))
                    .into(viewHolder.profileImage);

            int tmpUnread = db.getUnReadWith(myId,friendId,no,now) ;
            if(tmpUnread <=0) {
                viewHolder.rightUnread.setText("");
            }else{
                viewHolder.rightUnread.setText(tmpUnread+"");
            }

        }else if(chatMessageList.get(position).Type == 2){

            long now = chatMessageList.get(position).getTime();
            Date when = new Date(now);
            String time = sdfNow.format(when);

            viewHolder.layout.setGravity(Gravity.RIGHT);
            viewHolder.chatContent.setText(chatMessageList.get(position).getContent());
            viewHolder.chatContent.setBackgroundResource(R.drawable.outchat);
            viewHolder.nickName.setGravity(Gravity.RIGHT);
            viewHolder.leftLayout.setGravity(Gravity.RIGHT);
            viewHolder.leftText.setText(time);
            viewHolder.profileImage.setVisibility(View.GONE);
            viewHolder.nickName.setVisibility(View.GONE);
            viewHolder.chatContent.setVisibility(View.VISIBLE);
            viewHolder.leftLayout.setVisibility(View.VISIBLE);
            viewHolder.rightLayout.setVisibility(View.GONE);
            friendId = chatMessageList.get(position).getUserId();
            profile = chatMessageList.get(position).getProfile();
            Glide.with(mContext).load("http://vnschat.vps.phps.kr/profile_image/"+friendId+".png")
                    .error(R.drawable.default_profile_image)
                    .signature(new StringSignature(profile))
                    .into(viewHolder.profileImage);

            int tmpUnread = db.getUnReadWith(myId,no,now) ;
            if(tmpUnread <=0) {
                viewHolder.leftUnread.setText("");
            }else{
                viewHolder.leftUnread.setText(tmpUnread+"");
            }

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
