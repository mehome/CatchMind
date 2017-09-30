package com.catchmind.catchmind;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by sonsch94 on 2017-09-27.
 */

public class ImageEnlargeActivity extends Activity {

    public ImageView IV;
    public LinearLayout IE_ButtonSet;
    public LinearLayout BackBtnContainer;
    public Button Back_Btn;
    public RelativeLayout IE_Whole;
    private AQuery aq;
    private String svImage;
    private String aqUrl;
    private File aqFile;

    int no;
    long time;
    String friendId;
    int position;

    public MyDatabaseOpenHelper db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_enlarge);
        IV = (ImageView)findViewById(R.id.ImageEnlargeIV);
        IE_ButtonSet = (LinearLayout)findViewById(R.id.IE_ButtonSet);
        BackBtnContainer = (LinearLayout)findViewById(R.id.Back_Btn_Container);
        Back_Btn = (Button)findViewById(R.id.IE_back_btn);
        IE_Whole = (RelativeLayout)findViewById(R.id.IE_whole);

        Intent intent = getIntent();
        String IV_addr = intent.getExtras().getString("IV");
        no = intent.getExtras().getInt("no");
        time = intent.getExtras().getLong("time");
        friendId = intent.getExtras().getString("friendId");
        position = intent.getExtras().getInt("position");

        db = new MyDatabaseOpenHelper(this,"catchMind",null,1);


        Glide.with(this).load(IV_addr)
                .error(R.drawable.default_profile_image)
                .signature(new StringSignature(String.valueOf(System.currentTimeMillis())))
                .into(IV);


        IE_Whole.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(IE_ButtonSet.getVisibility() == View.GONE) {
                    IE_ButtonSet.setVisibility(View.VISIBLE);
                    BackBtnContainer.setVisibility(View.VISIBLE);
                }else{
                    IE_ButtonSet.setVisibility(View.GONE);
                    BackBtnContainer.setVisibility(View.GONE);
                }

            }
        });


        aq = new AQuery(this);
        aqUrl = IV_addr;
        aqFile = new File(Environment.getExternalStorageDirectory() + "/catchMind_Image/" + System.currentTimeMillis() + ".jpg");


    }


    public void SaveImage(View v){


        aq.download(aqUrl, aqFile, new AjaxCallback<File>() {
            @Override
            public void callback(String url, File object, AjaxStatus status) {
                if (object != null) {

                    Toast.makeText(ImageEnlargeActivity.this, "저장했습니다", Toast.LENGTH_SHORT).show();

                    // sendBroadcast를 통해 Crop된 사진을 앨범에 보이도록 갱신한다.

                    getApplicationContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(aqFile)));

                }else{

                    Toast.makeText(ImageEnlargeActivity.this, "저장에 실패했습니다", Toast.LENGTH_SHORT).show();

                }
            }
        });


    }


    public void ShareImage(View v){



        IV.buildDrawingCache();
        Bitmap icon = IV.getDrawingCache();
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/*");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        icon.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        File f = new File(Environment.getExternalStorageDirectory() + File.separator + "temporary_file.jpg");

        try {
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }

        share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///sdcard/temporary_file.jpg"));
        startActivity(Intent.createChooser(share, "Share Image"));



    }


    public void IE_finish(View v){



        finish();



    }



    public void IE_delete(View v){



        DialogInterface.OnClickListener deleteListener = new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which){
                db.deleteMessageData(no,friendId,time);

                Intent resultIntent = new Intent();

                resultIntent.putExtra("position" , position);

                setResult(RESULT_OK,resultIntent);

                finish();
            }

        };


        DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener(){

            @Override public void onClick(DialogInterface dialog, int which){
                dialog.dismiss();
            }

        };




        AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage("선택한 메시지를 삭제하시겠습니까 \n \n 삭제한 메시지는 내 채팅방에서만 적용되며 상대방의 채팅방에서는 삭제되지 않습니다.")
                .setPositiveButton("확인", deleteListener)
                .setNegativeButton("취소", cancelListener)
                .create();


        dialog.show();


        Button deleteBtn = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        deleteBtn.setTextColor(Color.BLACK);

        Button cancelBtn = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        cancelBtn.setTextColor(Color.BLACK);




    }



}
