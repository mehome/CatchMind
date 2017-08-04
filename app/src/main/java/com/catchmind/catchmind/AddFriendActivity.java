package com.catchmind.catchmind;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
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
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by sonsch94 on 2017-07-30.
 */

public class AddFriendActivity extends AppCompatActivity {

    Toolbar toolbar;

    Button idAddbtn;
    Button nicknameAddbtn;

    FrameLayout idFL;
    FrameLayout nicknameFL;
    String idData;
    String nicknameData;
    String idId;
    String nicknameId;
    EditText idEdit;
    EditText nicknameEdit;
    View noData,noData2;
    TextView noDatatxt,noDatatxt2;

    View friendView,friendView2;

    ImageView profileView,profileView2;
    TextView nameView,nameView2;
    TextView messageView,messageView2;
    LinearLayout sectionLinear,sectionLinear2;
    public MyDatabaseOpenHelper db;
    public SharedPreferences mPref;
    String myId;
    ArrayList<String> friendList = new ArrayList<String>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        toolbar = (Toolbar) findViewById(R.id.toolbarAddFriend);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("친구추가");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        db = new MyDatabaseOpenHelper(this,"catchMind",null,1);
        mPref = getSharedPreferences("login",MODE_PRIVATE);
        myId = mPref.getString("userId","아이디없음");

        Cursor cursor = db.getList();

        while(cursor.moveToNext()) {

            friendList.add(cursor.getString(0));
            Log.d("친구목록", cursor.getString(0)+"#####"+cursor.getString(1) + "" +cursor.getString(2));
        }


        idEdit = (EditText) findViewById(R.id.idfindedit);
        nicknameEdit = (EditText) findViewById(R.id.nicknamefindedit);

        noData = getLayoutInflater().inflate(R.layout.nodata,null);
        noData2 = getLayoutInflater().inflate(R.layout.nodata,null);

        noDatatxt = (TextView)noData.findViewById(R.id.nodatatxt);
        noDatatxt2 = (TextView)noData2.findViewById(R.id.nodatatxt);

        friendView = getLayoutInflater().inflate(R.layout.listview_item,null);
        friendView2 = getLayoutInflater().inflate(R.layout.listview_item,null);

        profileView = (ImageView) friendView.findViewById(R.id.profile_image);
        nameView = (TextView) friendView.findViewById(R.id.textView1);
        messageView = (TextView) friendView.findViewById(R.id.textView2);
        sectionLinear = (LinearLayout) friendView.findViewById(R.id.sectionHeader);
        sectionLinear.setVisibility(View.GONE);

        profileView2 = (ImageView) friendView2.findViewById(R.id.profile_image);
        nameView2 = (TextView) friendView2.findViewById(R.id.textView1);
        messageView2 = (TextView) friendView2.findViewById(R.id.textView2);
        sectionLinear2 = (LinearLayout) friendView2.findViewById(R.id.sectionHeader);
        sectionLinear2.setVisibility(View.GONE);

        idAddbtn = (Button) findViewById(R.id.idaddbtn);
        nicknameAddbtn = (Button) findViewById(R.id.nicknameaddbtn);

        idFL = (FrameLayout) findViewById(R.id.idFL);
        nicknameFL = (FrameLayout) findViewById(R.id.nicknameFL);




    }


    public void idSearch(View v){

        FindThread ft = new FindThread(idEdit.getText().toString(),"id");
        ft.start();

        try{
            ft.join();
        }catch(InterruptedException e){
            e.printStackTrace();
        }

        idFL.removeAllViewsInLayout();

        if(idData.equals("아이디")){
            noDatatxt.setText("검색결과가 없습니다");
            idFL.addView(noData);
            idAddbtn.setVisibility(View.INVISIBLE);

        }else{


            try {
                JSONObject jobject = new JSONObject(idData);

                String userId = jobject.getString("friendId");
                String name = jobject.getString("nickname");
                String message = jobject.getString("message");

                if(friendList.contains(userId)){
                    noDatatxt.setText("이미 친구입니다");
                    idFL.addView(noData);
                    idAddbtn.setVisibility(View.INVISIBLE);
                    return;
                }

                if(userId.equals(myId)){
                    noDatatxt.setText("본인아이디입니다");
                    idFL.addView(noData);
                    idAddbtn.setVisibility(View.INVISIBLE);
                    return;
                }

                nameView.setText(name);
                messageView.setText(message);

                String profile = jobject.getString("profile");

                Glide.with(this).load("http://vnschat.vps.phps.kr/profile_image/"+userId+".png")
                        .error(R.drawable.default_profile_image)
                        .signature(new StringSignature(String.valueOf(System.currentTimeMillis())))
                        .into(profileView);

                idFL.addView(friendView);
                idAddbtn.setVisibility(View.VISIBLE);

            }catch (JSONException e){
                e.printStackTrace();
            }

        }

    }

    public void idAddBtn(View v){

        try {
            JSONObject jobject = new JSONObject(idData);

            String userId = jobject.getString("friendId");
            String name = jobject.getString("nickname");
            String message = jobject.getString("message");
            String profile = jobject.getString("profile");

            if(friendList.contains(userId)){
                idFL.removeAllViewsInLayout();
                noDatatxt.setText("이미 친구입니다");
                idFL.addView(noData);
                idAddbtn.setVisibility(View.INVISIBLE);
                return;
            }

            AddThread at = new AddThread(userId);
            at.start();

            db.insert(userId, name, profile, message, 0);
            friendList.add(userId);

            idFL.removeAllViewsInLayout();
            idAddbtn.setVisibility(View.INVISIBLE);

        }catch (JSONException e){
            e.printStackTrace();
        }

    }

    public void nicknameSearch(View v){

        FindThread ft = new FindThread(nicknameEdit.getText().toString(),"nickname");
        ft.start();

        try{
            ft.join();
        }catch(InterruptedException e){
            e.printStackTrace();
        }

        nicknameFL.removeAllViewsInLayout();

        if(nicknameData.equals("아이디")){
            noDatatxt2.setText("검색결과가 없습니다");
            nicknameFL.addView(noData2);
            nicknameAddbtn.setVisibility(View.INVISIBLE);

        }else{

            try {
                JSONObject jobject = new JSONObject(nicknameData);

                String userId = jobject.getString("friendId");
                String name = jobject.getString("nickname");
                String message = jobject.getString("message");

                if(friendList.contains(userId)){
                    noDatatxt2.setText("이미 친구입니다");
                    nicknameFL.addView(noData2);
                    nicknameAddbtn.setVisibility(View.INVISIBLE);
                    return;
                }

                if(userId.equals(myId)){
                    noDatatxt2.setText("본인닉네임입니다");
                    nicknameFL.addView(noData2);
                    nicknameAddbtn.setVisibility(View.INVISIBLE);
                    return;
                }

                nameView2.setText(name);
                messageView2.setText(message);

                String profile = jobject.getString("profile");

                Glide.with(this).load("http://vnschat.vps.phps.kr/profile_image/"+userId+".png")
                        .error(R.drawable.default_profile_image)
                        .signature(new StringSignature(String.valueOf(System.currentTimeMillis())))
                        .into(profileView2);

                nicknameFL.addView(friendView2);
                nicknameAddbtn.setVisibility(View.VISIBLE);

            }catch (JSONException e){
                e.printStackTrace();
            }

        }

    }

    public void nickAddBtn(View v){
        try {
            JSONObject jobject = new JSONObject(nicknameData);

            String userId = jobject.getString("friendId");
            String name = jobject.getString("nickname");
            String message = jobject.getString("message");
            String profile = jobject.getString("profile");

            if(friendList.contains(userId)){
                nicknameFL.removeAllViewsInLayout();
                noDatatxt2.setText("이미 친구입니다");
                nicknameFL.addView(noData2);
                nicknameAddbtn.setVisibility(View.INVISIBLE);
                return;
            }

            AddThread at = new AddThread(userId);
            at.start();

            db.insert(userId, name, profile, message, 0);
            friendList.add(userId);

            nicknameFL.removeAllViewsInLayout();
            nicknameAddbtn.setVisibility(View.INVISIBLE);

        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    public class FindThread extends Thread {

        String sdata;
        String sMode;

        public FindThread(String userData,String mode) {
            this.sdata = userData;
            this.sMode = mode;
        }


        @Override
        public void run() {

            String data="";

            /* 인풋 파라메터값 생성 */
            String param = "userData="+ sdata + "&mode=" +sMode +"";
            try {
            /* 서버연결 */
                URL url = new URL("http://vnschat.vps.phps.kr/findFriend.php");
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
                Log.d("파인드스레드결과",data.toString());

                if(sMode.equals("id")) {
                    idData = data;
                }else{
                    nicknameData = data;
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }

    }

    public class AddThread extends Thread {

        String sUserId;

        public AddThread(String userId) {
            this.sUserId = userId;
        }


        @Override
        public void run() {

            String data="";

            /* 인풋 파라메터값 생성 */
            String param = "userId="+ myId + "&friendId=" + sUserId ;

            try {
            /* 서버연결 */
                URL url = new URL("http://vnschat.vps.phps.kr/addFriend.php");
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
                Log.d("에드스레드결과",data.toString());

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }

    }





    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }

}
