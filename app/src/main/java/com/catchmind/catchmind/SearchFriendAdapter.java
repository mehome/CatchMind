package com.catchmind.catchmind;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;

import java.util.ArrayList;

/**
 * Created by sonsch94 on 2017-09-22.
 */

public class SearchFriendAdapter extends BaseAdapter {

    public ArrayList<SearchFriendItem> SearchFriendList = new ArrayList<SearchFriendItem>() ;
    public Context mContext;
    public LayoutInflater inflater ;


    public SearchFriendAdapter(Context context,ArrayList<SearchFriendItem> searchFriendList) {

        this.mContext = context;
        this.inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.SearchFriendList = searchFriendList;

    }

    public void clearList(){
        this.SearchFriendList = new ArrayList<>();
    }

    public void addSFItem(SearchFriendItem addItem){
        this.SearchFriendList.add(addItem);
    }

    @Override
    public int getCount() {

        return SearchFriendList.size();

    }



    // position에 위치한 데이터를 화면에 출력하는데 사용될 View를 리턴. : 필수 구현
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        nameViewHolder viewHolder;
        String userId = "";
        String nickname = "";
        String profile = "";
        String message = "";


        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
        if (convertView == null) {

            convertView = this.inflater.inflate(R.layout.search_friend_item, parent, false);

            viewHolder = new nameViewHolder();
            viewHolder.icon = (ImageView) convertView.findViewById(R.id.profile_image_search);
            viewHolder.name = (TextView) convertView.findViewById(R.id.textView1_search);
            viewHolder.message = (TextView) convertView.findViewById(R.id.textView2_search);

            convertView.setTag(viewHolder);

        }else{

            viewHolder = (nameViewHolder) convertView.getTag();

        }


        userId = SearchFriendList.get(position).getId();
        nickname = SearchFriendList.get(position).getName();
        profile = SearchFriendList.get(position).getProfile();
        message = SearchFriendList.get(position).getMessage();


        viewHolder.name.setText(nickname);
        viewHolder.message.setText(message);


        if(profile.equals("none")){

            viewHolder.icon.setImageResource(R.drawable.default_profile_image);

        }else {

            Glide.with(mContext).load("http://vnschat.vps.phps.kr/profile_image/" + userId + ".png")
                    .error(R.drawable.default_profile_image)
                    .signature(new StringSignature(profile))
                    .into(viewHolder.icon);

        }


        convertView.setTag(R.id.userId,userId);
        convertView.setTag(R.id.nickname,nickname);
        convertView.setTag(R.id.profile,profile);

//        convertView.setTag(R.id.checkIcon,viewHolder.check);

        Log.d("SearchFriendAdapter",""+position);

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
        return SearchFriendList.get(position) ;
    }

}
