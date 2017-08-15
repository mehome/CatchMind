package com.catchmind.catchmind;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.util.Log;
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

import org.json.JSONArray;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ChatRoomAdapter extends BaseAdapter{

    public ListViewItem MyProfile;
    public ArrayList<ChatRoomItem> chatRoomList = new ArrayList<ChatRoomItem>() ;
    public Context mContext;
    public LayoutInflater inflater ;
    public MyDatabaseOpenHelper db;
    public String userId;
    public SimpleDateFormat sdfNow ;

    // ListViewAdapter의 생성자
    public ChatRoomAdapter(Context context,ArrayList<ChatRoomItem> ListData,String myId ) {
        this.mContext = context;
        this.inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.chatRoomList = ListData;
        this.userId = myId;
        this.sdfNow = new SimpleDateFormat("HH:mm");
        db = new MyDatabaseOpenHelper(mContext,"catchMind",null,1);
    }

    public void setChatRoomList(ArrayList<ChatRoomItem> ListData) {
        this.chatRoomList = ListData;
    }

    // Adapter에 사용되는 데이터의 개수를 리턴. : 필수 구현
    @Override
    public int getCount() {
        return chatRoomList.size();
    }


    // position에 위치한 데이터를 화면에 출력하는데 사용될 View를 리턴. : 필수 구현
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        chatViewHolder viewHolder;

        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
        if (convertView == null) {
            convertView = this.inflater.inflate(R.layout.chatroom_item, parent, false);

            viewHolder = new chatViewHolder();
            viewHolder.title = (TextView) convertView.findViewById(R.id.chatRoomTitle);
            viewHolder.content = (TextView) convertView.findViewById(R.id.chatRoomContent);
            viewHolder.memberNum = (TextView) convertView.findViewById(R.id.chatRoomMemberNum);
            viewHolder.time = (TextView) convertView.findViewById(R.id.chatRoomDate);
            viewHolder.profileImage = (ImageView) convertView.findViewById(R.id.chatRoomImage);
            viewHolder.unRead = (TextView) convertView.findViewById(R.id.unRead);

            convertView.setTag(viewHolder);

        }else{
            viewHolder = (chatViewHolder) convertView.getTag();
        }


        String content = "";
        long when = 0;
        long myWhen = 0;

        Cursor cursor = db.getLastRowJoinOnChatRoomList(userId,chatRoomList.get(position).getFriendId(),chatRoomList.get(position).getNo());
        cursor.moveToNext();
        content = cursor.getString(3);
        when = cursor.getLong(4);
        myWhen = cursor.getLong(8);
        Log.d("myWhen_"+position,myWhen+"");
        int unRead = db.getUnRead(userId,chatRoomList.get(position).getFriendId(),chatRoomList.get(position).getNo(),myWhen);

        if(unRead==0){
            viewHolder.unRead.setVisibility(View.INVISIBLE);
        }else{
            viewHolder.unRead.setVisibility(View.VISIBLE);
            viewHolder.unRead.setText(unRead+"");
        }

        Date recvTime = new Date(when);
        String time = this.sdfNow.format(recvTime);

        if(chatRoomList.get(position).getNo()==0) {

            Cursor userCS = db.getChatFriendListByIdAndNo(userId,chatRoomList.get(position).getNo(),chatRoomList.get(position).getFriendId());
            userCS.moveToNext();

            String nickname = userCS.getString(2);
            String profile = userCS.getString(3);

            viewHolder.title.setText(nickname);
            viewHolder.content.setText(content);
            viewHolder.time.setText(time);
            viewHolder.memberNum.setText("" + chatRoomList.get(position).getMemberNum());
            Glide.with(mContext).load("http://vnschat.vps.phps.kr/profile_image/" + chatRoomList.get(position).getFriendId() + ".png")
                    .error(R.drawable.default_profile_image)
                    .signature(new StringSignature(profile))
                    .into(viewHolder.profileImage);

            convertView.setTag(R.id.userId, chatRoomList.get(position).getFriendId());
            convertView.setTag(R.id.nickname, nickname);
            convertView.setTag(R.id.no, chatRoomList.get(position).getNo());
        }else{
            viewHolder.title.setText("그룹채팅 "+chatRoomList.get(position).getNo());
            viewHolder.content.setText(content);
            viewHolder.time.setText(time);
//            viewHolder.memberNum.setText("" + chatRoomList.get(position).getMemberNum());
            viewHolder.profileImage.setImageResource(R.drawable.group_icon);

            JSONArray jarray = new JSONArray();
            Cursor cs = db.getChatFriendListByNo(userId,chatRoomList.get(position).getNo());
            while(cs.moveToNext()) {

                jarray.put(cs.getString(1));

                Log.d("chatRoomAdapter","cs?"+cs.getString(1));
            }

            convertView.setTag(R.id.userId, jarray.toString());
            convertView.setTag(R.id.nickname, "그룹채팅 "+chatRoomList.get(position).getNo());
            convertView.setTag(R.id.no, chatRoomList.get(position).getNo());
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
        return chatRoomList.get(position) ;
    }


    public void ChangeList(ArrayList<ChatRoomItem> ListData){
        this.chatRoomList = ListData;
    }




    // 아이템 데이터 추가를 위한 함수. 개발자가 원하는대로 작성 가능.
//    public void addItem(String title, String content, int memberNum, String date) {
//        ChatRoomItem item = new ChatRoomItem(title,content,memberNum, date);
//
//        chatRoomList.add(item);
//    }
}
