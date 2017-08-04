
package com.catchmind.catchmind;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class FriendListViewAdapter extends BaseAdapter {
    // Adapter에 추가된 데이터를 저장하기 위한 ArrayList

    public ArrayList<ListViewItem> listViewItemList = new ArrayList<ListViewItem>() ;
    public ArrayList<ListViewItem> FlistViewItemList = new ArrayList<ListViewItem>() ;
    public ArrayList<String> BookmarkList = new ArrayList<String>();
    public Context mContext;
    public LayoutInflater inflater ;
    public int FlistSize;
    public int listSize;
    View tmp_view;
    public MyDatabaseOpenHelper db;
    public SharedPreferences mPref;
    String myId;
    HashMap<String, Integer> map = new HashMap<String, Integer>();


    // ListViewAdapter의 생성자
    public FriendListViewAdapter(Context context,ArrayList<ListViewItem> FListData,ArrayList<ListViewItem> ListData ,ArrayList<String> BookmarkData) {
        this.mContext = context;
        this.inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.listViewItemList = ListData;
        this.FlistViewItemList = FListData;
        this.listSize = ListData.size();
        this.FlistSize = FListData.size();
        this.db = new MyDatabaseOpenHelper(mContext,"catchMind",null,1);
        this.mPref = mContext.getSharedPreferences("login",MODE_PRIVATE);
        this.myId = this.mPref.getString("userId","아이디없음");
        this.BookmarkList = BookmarkData;
        FIndexReset();
    }

    public void FIndexReset(){

        map = new HashMap<String,Integer>();

        for(int i=0;i<this.FlistSize;i++){
            map.put(FlistViewItemList.get(i).getId(),i);
        }

    }

    final View.OnClickListener deleteListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            tmp_view = v;
            String userId = (String) tmp_view.getTag(R.id.userId);
            String nickname = (String) tmp_view.getTag(R.id.nickname);


            AlertDialog.Builder alt_bld = new AlertDialog.Builder(mContext);
            alt_bld.setCancelable(true);
            alt_bld.setTitle("알림");
            alt_bld.setMessage(nickname+"님을 친구목록에서 삭제하시겠습니까?");
            alt_bld.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id){

                    dialog.cancel();

                }
            });
            alt_bld.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {


                    String userId = (String) tmp_view.getTag(R.id.userId);
                    int index = (int)tmp_view.getTag(R.id.index);
                    listViewItemList.remove(index);
                    db.deleteByUserId(userId);
                    if(map.containsKey(userId)) {
                        int tmpIndex = (int) map.get(userId);
                        FlistViewItemList.remove(tmpIndex);
                    }
                    Log.d("피곤테스트",map.get(userId)+"");
                    sizeReset();
                    FIndexReset();
                    DeleteThread dt = new DeleteThread(myId,userId);
                    dt.start();
                    notifyDataSetChanged();


                }
             });

            AlertDialog alert = alt_bld.create();

            alert.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            alert.setCanceledOnTouchOutside(false);
            alert.show();

            Button pbtn = alert.getButton(DialogInterface.BUTTON_POSITIVE);
            pbtn.setTextColor(Color.BLACK);

            Button negbtn = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
            negbtn.setTextColor(Color.BLACK);
        }
    };

    final View.OnClickListener bookmarkListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            tmp_view = v;
            String userId = (String) tmp_view.getTag(R.id.userId);
            String nickname = (String) tmp_view.getTag(R.id.nickname);
            String mode = (String) tmp_view.getTag(R.id.bookmarkmode);
            int index = (int) tmp_view.getTag(R.id.index);

            if(mode.equals("add")){

                if (BookmarkList.contains(userId)) {


                    AlertDialog.Builder alt_bld = new AlertDialog.Builder(mContext);
                    alt_bld.setCancelable(true);
                    alt_bld.setTitle("알림");
                    alt_bld.setMessage(nickname + "님은 이미 즐겨찾기에 추가되어있습니다");
                    alt_bld.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            dialog.cancel();
                        }
                    });

                    AlertDialog alert = alt_bld.create();

                    alert.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                    alert.setCanceledOnTouchOutside(false);
                    alert.show();

                    Button pbtn = alert.getButton(DialogInterface.BUTTON_POSITIVE);
                    pbtn.setTextColor(Color.BLACK);

                    return;

                }

                ListViewItem addItem = (ListViewItem) tmp_view.getTag(R.id.bookmarkitem);
                FlistViewItemList.add(addItem);
                BookmarkList.add(userId);
                BookmarkThread bt = new BookmarkThread(myId,userId,1);
                bt.start();
                sizeReset();
                FIndexReset();
                db.addBookmark(userId);
                notifyDataSetChanged();

            }else{

                FlistViewItemList.remove(index);
                db.removeBookmark(userId);
                BookmarkList.remove(userId);
                sizeReset();
                FIndexReset();
                BookmarkThread bt = new BookmarkThread(myId,userId,0);
                bt.start();
                notifyDataSetChanged();

            }



        }
    };



    public void setListViewItemList(ArrayList<ListViewItem> ListData) {
        this.listViewItemList = ListData;
    }

    public void setFListViewItemList(ArrayList<ListViewItem> FListData) {
        this.FlistViewItemList = FListData;
    }

    public void sizeReset(){
        this.listSize = listViewItemList.size();
        this.FlistSize = FlistViewItemList.size();
    }

    // Adapter에 사용되는 데이터의 개수를 리턴. : 필수 구현
    @Override
    public int getCount() {
        int total;
        if(FlistSize >0){
            total = 1+FlistSize+1+listSize;
        }else{
            total = 1+listSize;
        }
        return total ;
    }

//    @Override
//    public boolean isEnabled(int position) {
//       if(position <5){
//           return false;
//       }else{
//           return true;
//       }
//    }

    // position에 위치한 데이터를 화면에 출력하는데 사용될 View를 리턴. : 필수 구현
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        friendViewHolder viewHolder;
        String userId = "";
        String nickname = "";
        String profile = "";


        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
        if (convertView == null) {
            convertView = this.inflater.inflate(R.layout.friend_list_view_item, parent, false);

            viewHolder = new friendViewHolder();
            viewHolder.icon = (ImageView) convertView.findViewById(R.id.profile_image);
            viewHolder.name = (TextView) convertView.findViewById(R.id.textView1);
            viewHolder.section = (LinearLayout) convertView.findViewById(R.id.sectionHeader);
            viewHolder.sectionTxt = (TextView) convertView.findViewById(R.id.sectionText);
            viewHolder.Deletebtn = (Button) convertView.findViewById(R.id.friendDelete);
            viewHolder.Bookmarktn = (Button) convertView.findViewById(R.id.friendBookmark);
            viewHolder.profile_container = (RelativeLayout) convertView.findViewById(R.id.profile_container);

            viewHolder.Deletebtn.setOnClickListener(deleteListener);
            viewHolder.Bookmarktn.setOnClickListener(bookmarkListener);

            convertView.setTag(viewHolder);

        }else{
            viewHolder = (friendViewHolder) convertView.getTag();
        }



            if (this.FlistSize > 0) {

                if (position == 0) {
                    viewHolder.sectionTxt.setText("즐겨찾기");
                    viewHolder.section.setVisibility(View.VISIBLE);
                    viewHolder.profile_container.setVisibility(View.GONE);

                } else if (position < (1 + FlistSize)) {

                    viewHolder.name.setText(FlistViewItemList.get(position - 1).getName());
                    viewHolder.section.setVisibility(View.GONE);
                    viewHolder.profile_container.setVisibility(View.VISIBLE);
                    userId = FlistViewItemList.get(position-1).getId();
                    nickname = FlistViewItemList.get(position-1).getName();
                    profile = FlistViewItemList.get(position-1).getProfile();
                    if(profile.equals("none")){
                        viewHolder.icon.setImageResource(R.drawable.default_profile_image);
                    }else {
                        Glide.with(mContext).load("http://vnschat.vps.phps.kr/profile_image/" + userId + ".png")
                                .error(R.drawable.default_profile_image)
                                .signature(new StringSignature(profile))
                                .into(viewHolder.icon);
                    }
                    viewHolder.Deletebtn.setVisibility(View.GONE);

                    viewHolder.Bookmarktn.setTag(R.id.bookmarkmode,"remove");
                    viewHolder.Bookmarktn.setTag(R.id.userId,userId);
                    viewHolder.Bookmarktn.setTag(R.id.nickname,nickname);
                    viewHolder.Bookmarktn.setTag(R.id.index,position-1);
                    viewHolder.Bookmarktn.setText("즐겨찾기해제");


                } else if (position == (1 + FlistSize)) {
                    viewHolder.sectionTxt.setText("친구");
                    viewHolder.section.setVisibility(View.VISIBLE);
                    viewHolder.profile_container.setVisibility(View.GONE);

                } else {
                    viewHolder.name.setText(listViewItemList.get(position - 2 - FlistSize).getName());
                    viewHolder.section.setVisibility(View.GONE);
                    viewHolder.profile_container.setVisibility(View.VISIBLE);
                    userId = listViewItemList.get(position-2-FlistSize).getId();
                    nickname = listViewItemList.get(position-2-FlistSize).getName();
                    profile = listViewItemList.get(position-2-FlistSize).getProfile();
                    if(profile.equals("none")){
                        viewHolder.icon.setImageResource(R.drawable.default_profile_image);
                    }else {
                        Glide.with(mContext).load("http://vnschat.vps.phps.kr/profile_image/" + userId + ".png")
                                .error(R.drawable.default_profile_image)
                                .signature(new StringSignature(profile))
                                .into(viewHolder.icon);
                    }
                    viewHolder.Deletebtn.setVisibility(View.VISIBLE);
                    viewHolder.Deletebtn.setTag(R.id.userId,userId);
                    viewHolder.Deletebtn.setTag(R.id.nickname,nickname);
                    viewHolder.Deletebtn.setTag(R.id.index,position-2-FlistSize);

                    viewHolder.Bookmarktn.setTag(R.id.bookmarkitem,listViewItemList.get(position-2-FlistSize));
                    viewHolder.Bookmarktn.setTag(R.id.bookmarkmode,"add");
                    viewHolder.Bookmarktn.setTag(R.id.userId,userId);
                    viewHolder.Bookmarktn.setTag(R.id.nickname,nickname);
                    viewHolder.Bookmarktn.setTag(R.id.index,position-2-FlistSize);
                    viewHolder.Bookmarktn.setText("즐겨찾기등록");

                }


            } else {

                if (position == 0) {
                    viewHolder.sectionTxt.setText("친구");
                    viewHolder.section.setVisibility(View.VISIBLE);
                    viewHolder.profile_container.setVisibility(View.GONE);

                } else {
                    viewHolder.name.setText(listViewItemList.get(position - 1).getName());
                    viewHolder.section.setVisibility(View.GONE);
                    viewHolder.profile_container.setVisibility(View.VISIBLE);
                    userId = listViewItemList.get(position-1).getId();
                    nickname = listViewItemList.get(position-1).getName();
                    profile = listViewItemList.get(position-1).getProfile();

                    if(profile.equals("none")){
                        viewHolder.icon.setImageResource(R.drawable.default_profile_image);
                    }else {
                        Glide.with(mContext).load("http://vnschat.vps.phps.kr/profile_image/" + userId + ".png")
                                .error(R.drawable.default_profile_image)
                                .signature(new StringSignature(profile))
                                .into(viewHolder.icon);
                    }

                    viewHolder.Deletebtn.setVisibility(View.VISIBLE);
                    viewHolder.Deletebtn.setTag(R.id.userId,userId);
                    viewHolder.Deletebtn.setTag(R.id.nickname,nickname);
                    viewHolder.Deletebtn.setTag(R.id.index,position-1);

                    viewHolder.Bookmarktn.setTag(R.id.bookmarkitem,listViewItemList.get(position-1));
                    viewHolder.Bookmarktn.setTag(R.id.bookmarkmode,"add");
                    viewHolder.Bookmarktn.setTag(R.id.userId,userId);
                    viewHolder.Bookmarktn.setTag(R.id.nickname,nickname);
                    viewHolder.Bookmarktn.setTag(R.id.index,position-1);
                    viewHolder.Bookmarktn.setText("즐겨찾기등록");

                }

            }



        convertView.setTag(R.id.userId,userId);
        convertView.setTag(R.id.nickname,nickname);
        convertView.setTag(R.id.profile,profile);

        Log.d("힘들다",""+position);

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
        return listViewItemList.get(position) ;
    }

    public class DeleteThread extends Thread {

        String sUserId;
        String sFriendId;

        public DeleteThread(String userId,String friendId) {
            this.sUserId = userId;
            this.sFriendId = friendId;
        }


        @Override
        public void run() {

            String data="";

            /* 인풋 파라메터값 생성 */
            String param = "userId=" + sUserId + "&friendId=" + sFriendId + "";
            try {
            /* 서버연결 */
                URL url = new URL("http://vnschat.vps.phps.kr/deleteFriend.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.connect();

            /* 안드로이드 -> 서버 파라메터값 전달 */
                OutputStream outs = conn.getOutputStream();
                outs.write(param.getBytes("UTF-8"));
                outs.flush();
                outs.close();

                InputStream is = null;
                BufferedReader in = null;

                is = conn.getInputStream();
                in = new BufferedReader(new InputStreamReader(is), 8 * 1024);
                String line = null;
                StringBuffer buff = new StringBuffer();
                while ( ( line = in.readLine() ) != null )
                {
                    buff.append(line + "\n");
                }
                data = buff.toString().trim();
                Log.d("삭제스레드결과",data.toString());

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }

    }



    public class BookmarkThread extends Thread {

        String sUserId;
        String sFriendId;
        int sMode;

        public BookmarkThread(String userId,String friendId,int mode) {
            this.sUserId = userId;
            this.sFriendId = friendId;
            this.sMode = mode;
        }


        @Override
        public void run() {

            String data="";

            /* 인풋 파라메터값 생성 */
            String param = "userId=" + sUserId + "&friendId=" + sFriendId + "&mode=" +sMode +"";
            try {
            /* 서버연결 */
                URL url = new URL("http://vnschat.vps.phps.kr/friendBookmark.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.connect();

            /* 안드로이드 -> 서버 파라메터값 전달 */
                OutputStream outs = conn.getOutputStream();
                outs.write(param.getBytes("UTF-8"));
                outs.flush();
                outs.close();

                InputStream is = null;
                BufferedReader in = null;

                is = conn.getInputStream();
                in = new BufferedReader(new InputStreamReader(is), 8 * 1024);
                String line = null;
                StringBuffer buff = new StringBuffer();
                while ( ( line = in.readLine() ) != null )
                {
                    buff.append(line + "\n");
                }
                data = buff.toString().trim();
                Log.d("북마크스레드결과",data.toString());

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }

    }



    // 아이템 데이터 추가를 위한 함수. 개발자가 원하는대로 작성 가능.
//    public void addItem(String id,Drawable icon, String name, String message) {
//        ListViewItem item = new ListViewItem(id,name,message);
//
//        item.setIcon(icon);
//        item.setName(name);
//        item.setMessage(message);
//
//        listViewItemList.add(item);
//    }
}


