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

import java.util.ArrayList;

public class ListViewAdapter extends BaseAdapter {
    // Adapter에 추가된 데이터를 저장하기 위한 ArrayList
    public ListViewItem MyProfile;
    public ArrayList<ListViewItem> listViewItemList = new ArrayList<ListViewItem>() ;
    public ArrayList<ListViewItem> FlistViewItemList = new ArrayList<ListViewItem>() ;
    public Context mContext;
    public LayoutInflater inflater ;
    public int FlistSize;
    public int listSize;


    // ListViewAdapter의 생성자
    public ListViewAdapter(Context context,ListViewItem MyData,ArrayList<ListViewItem> FListData,ArrayList<ListViewItem> ListData ) {
        this.mContext = context;
        this.inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.listViewItemList = ListData;
        this.FlistViewItemList = FListData;
        this.listSize = ListData.size();
        this.FlistSize = FListData.size();
        this.MyProfile = MyData;

    }

    public void setListViewItemList(ArrayList<ListViewItem> ListData) {
        this.listViewItemList = ListData;
    }

    public void setFListViewItemList(ArrayList<ListViewItem> FListData) {
        this.FlistViewItemList = FListData;
    }

    public void changeMyItem(ListViewItem MyItem){
        this.MyProfile = MyItem;
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
            total = 2+1+FlistSize+1+listSize;
        }else{
            total = 2+1+listSize;
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

        nameViewHolder viewHolder;
        String userId = "";
        String nickname = "";
        String message = "";
        String profile = "";


        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
        if (convertView == null) {

            convertView = this.inflater.inflate(R.layout.listview_item, parent, false);


            viewHolder = new nameViewHolder();
            viewHolder.icon = (ImageView) convertView.findViewById(R.id.profile_image);
            viewHolder.name = (TextView) convertView.findViewById(R.id.textView1);
            viewHolder.message = (TextView) convertView.findViewById(R.id.textView2);
            viewHolder.section = (LinearLayout) convertView.findViewById(R.id.sectionHeader);
            viewHolder.sectionTxt = (TextView) convertView.findViewById(R.id.sectionText);
            viewHolder.profile_container = (RelativeLayout) convertView.findViewById(R.id.profile_container);


            convertView.setTag(viewHolder);

        }else{

            viewHolder = (nameViewHolder) convertView.getTag();

        }

        if(position ==0){

            viewHolder.sectionTxt.setText("내 프로필");
            viewHolder.section.setVisibility(View.VISIBLE);
            viewHolder.profile_container.setVisibility(View.GONE);

        }else if(position == 1){

            viewHolder.name.setText(MyProfile.getName());
            viewHolder.message.setText(MyProfile.getMessage());
            viewHolder.section.setVisibility(View.GONE);
            viewHolder.profile_container.setVisibility(View.VISIBLE);
            userId = MyProfile.getId();
            nickname = MyProfile.getName();
            message = MyProfile.getMessage();
            profile = MyProfile.getProfile();
            Glide.with(mContext).load("http://vnschat.vps.phps.kr/profile_image/"+userId+".png")
                    .error(R.drawable.default_profile_image)
                    .signature(new StringSignature(String.valueOf(System.currentTimeMillis())))
                    .into(viewHolder.icon);

        }else{

            if (this.FlistSize > 0) {

                if (position == 2) {
                    viewHolder.sectionTxt.setText("즐겨찾기");
                    viewHolder.section.setVisibility(View.VISIBLE);
                    viewHolder.profile_container.setVisibility(View.GONE);

                } else if (position < (3 + FlistSize)) {

                    viewHolder.name.setText(FlistViewItemList.get(position - 3).getName());
                    viewHolder.message.setText(FlistViewItemList.get(position - 3).getMessage());
                    viewHolder.section.setVisibility(View.GONE);
                    viewHolder.profile_container.setVisibility(View.VISIBLE);
                    userId = FlistViewItemList.get(position-3).getId();
                    nickname = FlistViewItemList.get(position-3).getName();
                    message = FlistViewItemList.get(position-3).getMessage();
                    profile = FlistViewItemList.get(position-3).getProfile();
                    if(profile.equals("none")){
                        viewHolder.icon.setImageResource(R.drawable.default_profile_image);
                    }else {
                        Glide.with(mContext).load("http://vnschat.vps.phps.kr/profile_image/" + userId + ".png")
                                .error(R.drawable.default_profile_image)
                                .signature(new StringSignature(profile))
                                .into(viewHolder.icon);
                    }

                } else if (position == (3 + FlistSize)) {
                    viewHolder.sectionTxt.setText("친구");
                    viewHolder.section.setVisibility(View.VISIBLE);
                    viewHolder.profile_container.setVisibility(View.GONE);

                } else {
                    viewHolder.name.setText(listViewItemList.get(position - 4 - FlistSize).getName());
                    viewHolder.message.setText(listViewItemList.get(position - 4 - FlistSize).getMessage());
                    viewHolder.section.setVisibility(View.GONE);
                    viewHolder.profile_container.setVisibility(View.VISIBLE);
                    userId = listViewItemList.get(position-4-FlistSize).getId();
                    nickname = listViewItemList.get(position-4-FlistSize).getName();
                    message = listViewItemList.get(position-4-FlistSize).getMessage();
                    profile = listViewItemList.get(position-4-FlistSize).getProfile();
                    if(profile.equals("none")){
                        viewHolder.icon.setImageResource(R.drawable.default_profile_image);
                        Log.d("여긴데3",userId);
                    }else {
                        Glide.with(mContext).load("http://vnschat.vps.phps.kr/profile_image/" + userId + ".png")
                                .error(R.drawable.default_profile_image)
                                .signature(new StringSignature(profile))
                                .into(viewHolder.icon);
                        Log.d("여긴데4",userId);
                    }
                }


            } else {

                if (position == 2) {

                    viewHolder.sectionTxt.setText("친구");
                    viewHolder.section.setVisibility(View.VISIBLE);
                    viewHolder.profile_container.setVisibility(View.GONE);
                    Log.d("여긴데친구",userId);

                } else {

                    viewHolder.name.setText(listViewItemList.get(position - 3).getName());
                    viewHolder.message.setText(listViewItemList.get(position - 3).getMessage());
                    viewHolder.section.setVisibility(View.GONE);
                    viewHolder.profile_container.setVisibility(View.VISIBLE);
                    userId = listViewItemList.get(position-3).getId();
                    nickname = listViewItemList.get(position-3).getName();
                    message = listViewItemList.get(position-3).getMessage();
                    profile = listViewItemList.get(position-3).getProfile();

                    if(profile.equals("none")){
                        viewHolder.icon.setImageResource(R.drawable.default_profile_image);
                        Log.d("여긴데1",userId);
                    }else {
                        Glide.with(mContext).load("http://vnschat.vps.phps.kr/profile_image/" + userId + ".png")
                                .error(R.drawable.default_profile_image)
                                .signature(new StringSignature(profile))
                                .into(viewHolder.icon);
                        Log.d("여긴데2",userId);
                    }

                }

            }

        }

        convertView.setTag(R.id.userId,userId);
        convertView.setTag(R.id.nickname,nickname);
        convertView.setTag(R.id.profile,profile);
        convertView.setTag(R.id.message,message);

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


    public void ChangeList(ArrayList<ListViewItem> FListData, ArrayList<ListViewItem> ListData){
        this.FlistViewItemList = FListData;
        this.listViewItemList = ListData;
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


