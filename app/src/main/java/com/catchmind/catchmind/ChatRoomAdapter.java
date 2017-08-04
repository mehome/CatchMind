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

import java.util.ArrayList;

public class ChatRoomAdapter extends BaseAdapter{

    public ListViewItem MyProfile;
    public ArrayList<ChatRoomItem> chatRoomList = new ArrayList<ChatRoomItem>() ;
    public Context mContext;
    public LayoutInflater inflater ;


    // ListViewAdapter의 생성자
    public ChatRoomAdapter(Context context,ArrayList<ChatRoomItem> ListData ) {
        this.mContext = context;
        this.inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.chatRoomList = ListData;
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
            viewHolder.date = (TextView) convertView.findViewById(R.id.chatRoomDate);

            convertView.setTag(viewHolder);

        }else{
            viewHolder = (chatViewHolder) convertView.getTag();
        }


        viewHolder.title.setText(chatRoomList.get(position).getTitle());
        viewHolder.content.setText(chatRoomList.get(position).getContent());
        viewHolder.memberNum.setText(""+chatRoomList.get(position).getMemberNum());
        viewHolder.date.setText(chatRoomList.get(position).getDate());

        convertView.setTag(R.id.conversation,chatRoomList.get(position).getTitle());

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

    // 아이템 데이터 추가를 위한 함수. 개발자가 원하는대로 작성 가능.
    public void addItem(String title, String content, int memberNum, String date) {
        ChatRoomItem item = new ChatRoomItem(title,content,memberNum, date);

        chatRoomList.add(item);
    }
}
