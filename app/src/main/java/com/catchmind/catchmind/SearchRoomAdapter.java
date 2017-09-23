package com.catchmind.catchmind;

/**
 * Created by sonsch94 on 2017-09-22.
 */

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.json.JSONArray;

import java.util.Date;

/**
 * Created by sonsch94 on 2017-09-22.
 */

public class SearchRoomAdapter extends BaseAdapter {


    public ArrayList<ChatRoomItem> SearchRoomList = new ArrayList<>() ;
    public Context mContext;
    public LayoutInflater inflater ;
    public MyDatabaseOpenHelper db;
    public String userId;
    public SimpleDateFormat sdfNow ;


    public SearchRoomAdapter(Context context, ArrayList<ChatRoomItem> searchRoomList, String myId) {

        this.mContext = context;
        this.inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.SearchRoomList = searchRoomList;
        db = new MyDatabaseOpenHelper(mContext,"catchMind",null,1);
        this.userId = myId;
        this.sdfNow = new SimpleDateFormat("HH:mm");

    }


    public void clearList(){
        this.SearchRoomList = new ArrayList<>();
    }


    public void addCRItem(ChatRoomItem addItem){
        this.SearchRoomList.add(addItem);
    }


    @Override
    public int getCount() {

        return SearchRoomList.size();

    }



    // position에 위치한 데이터를 화면에 출력하는데 사용될 View를 리턴. : 필수 구현
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        chatViewHolder viewHolder;


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
        int unRead = 0;
        Date recvTime;
        String time;

        Cursor cursor = db.getLastRowJoinOnChatRoomList(userId,SearchRoomList.get(position).getFriendId(),SearchRoomList.get(position).getNo());
        Log.d("헬이",cursor.getColumnName(5));
        if(cursor.getCount() != 0) {
            cursor.moveToNext();
            if(cursor.getInt(5) == 1 || cursor.getInt(5) == 2) {
                content = cursor.getString(3);
            }else{
                content = "<사진>";
            }
            when = cursor.getLong(4);
            myWhen = cursor.getLong(8);
            Log.d("myWhen_" + position, myWhen + "");
            unRead = db.getUnRead(userId, SearchRoomList.get(position).getFriendId(), SearchRoomList.get(position).getNo(), myWhen);
            recvTime = new Date(when);
            time = this.sdfNow.format(recvTime);
        }else{
            time = "";
        }

        cursor.close();

        if(unRead==0){
            viewHolder.unRead.setVisibility(View.INVISIBLE);
        }else{
            viewHolder.unRead.setVisibility(View.VISIBLE);
            viewHolder.unRead.setText(unRead+"");
        }



        if(SearchRoomList.get(position).getNo()==0) {

            Cursor userCS = db.getChatFriendListByIdAndNo(SearchRoomList.get(position).getNo(),SearchRoomList.get(position).getFriendId());
            String nickname = "";
            String profile = "";
            Log.d("가라",position+"###"+userCS.getCount());
            if(userCS.getCount() != 0) {
                userCS.moveToNext();

                nickname = userCS.getString(2);
                profile = userCS.getString(3);

                Log.d("가라",position+"###"+nickname);
                Log.d("가라",position+"###"+profile);
            }
            userCS.close();

            Log.d("오호1",SearchRoomList.get(position).getNo()+"###"+position);

            viewHolder.title.setText(nickname);
            viewHolder.content.setText(content);
            viewHolder.time.setText(time);
            viewHolder.memberNum.setText("" + SearchRoomList.get(position).getMemberNum());
            Glide.with(mContext).load("http://vnschat.vps.phps.kr/profile_image/" + SearchRoomList.get(position).getFriendId() + ".png")
                    .error(R.drawable.default_profile_image)
                    .signature(new StringSignature(profile))
                    .into(viewHolder.profileImage);

            convertView.setTag(R.id.userId, SearchRoomList.get(position).getFriendId());
            convertView.setTag(R.id.nickname, nickname);
            convertView.setTag(R.id.no, SearchRoomList.get(position).getNo());

        }else{
            viewHolder.title.setText("그룹채팅 "+SearchRoomList.get(position).getNo());
            viewHolder.content.setText(content);
            viewHolder.time.setText(time);
//            viewHolder.memberNum.setText("" + chatRoomList.get(position).getMemberNum());
            viewHolder.profileImage.setImageResource(R.drawable.group_icon);
            Log.d("오호2",SearchRoomList.get(position).getNo()+"###"+position);
            JSONArray jarray = new JSONArray();
            Cursor cs = db.getChatFriendListByNo(SearchRoomList.get(position).getNo());
            while(cs.moveToNext()) {

                jarray.put(cs.getString(1));

                Log.d("SearchRoomAdapter","cs?"+cs.getString(1));
            }
            cs.close();

            convertView.setTag(R.id.userId, jarray.toString());
            convertView.setTag(R.id.nickname, "그룹채팅 "+SearchRoomList.get(position).getNo());
            convertView.setTag(R.id.no, SearchRoomList.get(position).getNo());
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
        return SearchRoomList.get(position) ;
    }



}
