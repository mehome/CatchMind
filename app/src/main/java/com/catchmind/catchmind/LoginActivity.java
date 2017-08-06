package com.catchmind.catchmind;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by sonsch94 on 2017-07-15.
 */

public class LoginActivity extends AppCompatActivity{

    public EditText userId,password;
    public String sUserId,sPassword;
    public MyDatabaseOpenHelper db;
    public SharedPreferences mPref;
    public SharedPreferences.Editor editor;
    public CheckBox autoLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userId = (EditText) findViewById(R.id.userIdInput);
        password = (EditText) findViewById(R.id.passwordInput);
        autoLogin = (CheckBox) findViewById(R.id.autoLogin);

        sUserId = userId.getText().toString();
        sPassword = password.getText().toString();
        mPref = getSharedPreferences("login",MODE_PRIVATE);
        editor = mPref.edit();
//
//        Intent intent = new Intent(this,MainActivity.class);
//        startActivity(intent);
//        finish();

        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1234);
        }


        if(mPref.getBoolean("autoLogin",false) == true){

            sUserId = mPref.getString("autoLoginId","");
            sPassword = mPref.getString("autoLoginPassword","");

            loginAT LAT = new loginAT();
            LAT.execute();
        }

    }


    public void login(View view) {


        sUserId = userId.getText().toString();
        sPassword = password.getText().toString();

        loginAT LAT = new loginAT();
        LAT.execute();

    }

    public void signUp(View view){

        Intent intent = new Intent(this,SignUpActivity.class);
        startActivity(intent);

    }


    public class loginAT extends AsyncTask<Void, Integer, String> {


        ProgressDialog asyncDialog = new ProgressDialog(LoginActivity.this);

        @Override
        protected void onPreExecute() {
            asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            asyncDialog.setMessage("로그인 중입니다...");

            // show dialog
            asyncDialog.show();
            super.onPreExecute();
        }




        @Override
        protected String doInBackground(Void... unused) {


            try{
                Thread.sleep(1000);
            }catch(InterruptedException e){

            }


            String data="";

            /* 인풋 파라메터값 생성 */
            String param = "userId=" + sUserId + "&password=" + sPassword + "";
            try {
            /* 서버연결 */
                URL url = new URL("http://vnschat.vps.phps.kr/login.php");
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



            /* 서버 -> 안드로이드 파라메터값 전달 */
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
                Log.e("RECV DATA",data);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return data;
        }

        @Override
        protected void onPostExecute(String s){
            super.onPostExecute(s);
            asyncDialog.dismiss();
            login_check(s);

        }

    }

    public void login_check(String data){


        if(data.equals("비밀번호")){
            Toast.makeText(this,"비밀번호가 일치하지 않습니다",Toast.LENGTH_SHORT).show();
        }else if(data.equals("아이디")){
            Toast.makeText(this,"일치하는 아이디가 없습니다",Toast.LENGTH_SHORT).show();
        }else{
            try {

                db = new MyDatabaseOpenHelper(this, "catchMind", null, 1);

                Log.d("여긴 어떤가",sUserId);
                db.clearFriendList();
                db.createTable();
                db.createMessageData(sUserId);
                db.createChatFriendList(sUserId);
                db.createChatRoomList(sUserId);

//                db.insertChatFriendData(sUserId,0,"thdwndrl","송중기","xxx","상태메시지",1234);

//                db.insertMessageData(sUserId,"thdwndrl","안녕중기야","오후 5시20분",1);
//                db.insertMessageData(sUserId,"qkrqhdud","안녕보영아","오후 5시20분",1);


                Log.d("형태db",db.toString());
                Log.d("형태디비",db.getDatabaseName());



                Log.d("형태", data);
                JSONArray jarray = new JSONArray(data);
                Log.d("형태2", jarray.toString());

                for(int i=0;i<jarray.length();i++) {


                    JSONObject jobject = new JSONObject(jarray.get(i).toString());
                    Log.d("형태3", jobject.toString());
                    String friendId = (String) jobject.getString("friendId");
                    String nickname = (String) jobject.getString("nickname");
                    String profile = (String) jobject.getString("profile");
                    String message = (String) jobject.getString("message");
                    int bookmark = (int) jobject.getInt("bookmark");
                    Log.d("형태4",friendId+"#####"+nickname);

                    if(i==0){
                        editor.putString("userId",friendId);
                        editor.putString("nickname",nickname);
                        editor.putString("profile",profile);
                        editor.putString("message",message);
                        db.insertChatFriendData(sUserId,0,sUserId,nickname,profile,message,0);
                        editor.commit();
                    }else {
                        db.insert(friendId, nickname, profile, message, bookmark);
                    }
                }


                if(autoLogin.isChecked()){
                    editor.putBoolean("autoLogin",true);
                    editor.putString("autoLoginId",sUserId);
                    editor.putString("autoLoginPassword",sPassword);

                    editor.commit();
                }

                Intent serviceIntent = new Intent(getApplicationContext(),ChatService.class);
                startService(serviceIntent);

                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();

            }catch (JSONException e){
                e.printStackTrace();
            }
        }




    }

}
