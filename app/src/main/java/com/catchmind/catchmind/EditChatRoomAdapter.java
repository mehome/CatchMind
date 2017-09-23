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
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

public class EditChatRoomAdapter extends BaseAdapter{

    public ListViewItem MyProfile;
    public ArrayList<ChatRoomItem> chatRoomList = new ArrayList<ChatRoomItem>() ;
    public Context mContext;
    public LayoutInflater inflater ;
    public MyDatabaseOpenHelper db;
    public String userId;
    HashMap<String, Boolean> isChecked = new HashMap<>();

    // ListViewAdapter의 생성자
    public EditChatRoomAdapter(Context context,ArrayList<ChatRoomItem> ListData,String myId , HashMap<String, Boolean> IsChecked ) {
        this.mContext = context;
        this.inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.chatRoomList = ListData;
        this.userId = myId;
        this.isChecked = IsChecked;

        db = new MyDatabaseOpenHelper(mContext,"catchMind",null,1);

    }

    public void changeIsChecked(String userId){

        if(this.isChecked.get(userId)){
            this.isChecked.put(userId,false);
        }else{
            this.isChecked.put(userId,true);
        }

    }

    public void changeAll(boolean allCheck){

        Iterator<String> iterator = isChecked.keySet().iterator();

        while(iterator.hasNext()){

            isChecked.put(iterator.next(),allCheck);

        }


    }

    public String exitCheckedRoom(){

        Iterator<String> iterator = isChecked.keySet().iterator();

        JSONArray jarray = new JSONArray();

        while(iterator.hasNext()){
            try {

                String key = iterator.next();
                if (isChecked.get(key)) {

                    JSONObject jsonObject = new JSONObject();

                    if (Character.isDigit(key.charAt(0))) {

                        jsonObject.put("group", true);


                        Log.d("리시브ㅋㅋ",key);

                        int no = Integer.parseInt(key);

                        jsonObject.put("id",no);

                        String FIE = db.getChatFriendListByNoJarray(no);

                        jsonObject.put("FIE",FIE);

                        db.deleteRoom(no,key);
                        db.deleteChatFriendAll(no,key);
                        db.deleteMessageData(no,key);


                    } else {

                        jsonObject.put("group", false);
                        jsonObject.put("id",key);

                        db.deleteRoom(0,key);
                        db.deleteChatFriendAll(0,key);
                        db.deleteMessageData(0,key);

                    }

                    jarray.put(jsonObject);



                }

            }catch (JSONException e){
                e.printStackTrace();
            }
        }

        return jarray.toString();

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

            convertView = this.inflater.inflate(R.layout.chatroom_item_check, parent, false);

            viewHolder = new chatViewHolder();
            viewHolder.title = (TextView) convertView.findViewById(R.id.chatRoomTitle);
            viewHolder.content = (TextView) convertView.findViewById(R.id.chatRoomContent);
            viewHolder.memberNum = (TextView) convertView.findViewById(R.id.chatRoomMemberNum);
            viewHolder.profileImage = (ImageView) convertView.findViewById(R.id.chatRoomImage);
            viewHolder.check = (ImageView) convertView.findViewById(R.id.check_image_room);

            convertView.setTag(viewHolder);

        }else{
            viewHolder = (chatViewHolder) convertView.getTag();
        }


        String content = "";

        Cursor cursor = db.getLastRowJoinOnChatRoomList(userId,chatRoomList.get(position).getFriendId(),chatRoomList.get(position).getNo());
        Log.d("헬이",cursor.getColumnName(5));
        if(cursor.getCount() != 0) {
            cursor.moveToNext();
            if(cursor.getInt(5) == 1 || cursor.getInt(5) == 2) {
                content = cursor.getString(3);
            }else{
                content = "<사진>";
            }
        }

        cursor.close();

        if(chatRoomList.get(position).getNo()==0) {

            if(isChecked.get(chatRoomList.get(position).getFriendId())){
                viewHolder.check.setImageResource(R.drawable.check_icon);
            }else{
                viewHolder.check.setImageResource(R.drawable.check_icon_inact);
            }


            Cursor userCS = db.getChatFriendListByIdAndNo(chatRoomList.get(position).getNo(),chatRoomList.get(position).getFriendId());
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

            Log.d("오호1",chatRoomList.get(position).getNo()+"###"+position);

            viewHolder.title.setText(nickname);
            viewHolder.content.setText(content);
//            viewHolder.memberNum.setText("" + chatRoomList.get(position).getMemberNum());
            Glide.with(mContext).load("http://vnschat.vps.phps.kr/profile_image/" + chatRoomList.get(position).getFriendId() + ".png")
                    .error(R.drawable.default_profile_image)
                    .signature(new StringSignature(profile))
                    .into(viewHolder.profileImage);

            convertView.setTag(R.id.userId, chatRoomList.get(position).getFriendId());
            convertView.setTag(R.id.nickname, nickname);
            convertView.setTag(R.id.no, chatRoomList.get(position).getNo());

        }else{

            if(isChecked.get(chatRoomList.get(position).getNo()+"")){
                viewHolder.check.setImageResource(R.drawable.check_icon);
            }else{
                viewHolder.check.setImageResource(R.drawable.check_icon_inact);
            }

            viewHolder.title.setText("그룹채팅 "+chatRoomList.get(position).getNo());
            viewHolder.content.setText(content);
//            viewHolder.memberNum.setText("" + chatRoomList.get(position).getMemberNum());
            viewHolder.profileImage.setImageResource(R.drawable.group_icon);
            Log.d("오호2",chatRoomList.get(position).getNo()+"###"+position);
            JSONArray jarray = new JSONArray();
            Cursor cs = db.getChatFriendListByNo(chatRoomList.get(position).getNo());
            while(cs.moveToNext()) {

                jarray.put(cs.getString(1));

                Log.d("chatRoomAdapter","cs?"+cs.getString(1));
            }
            cs.close();

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
