package com.catchmind.catchmind;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by sonsch94 on 2017-09-28.
 */

public class MessageDeleteCopy extends Activity {

    int no;
    long time;
    String friendId;
    int position;

    public MyDatabaseOpenHelper db;

    TextView deleteTV;
    TextView copyTV;

    String subType;
    String content;

    LinearLayout WholeMDC;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_message_dc);


        deleteTV = (TextView)findViewById(R.id.MessageDC_DeleteTV);
        copyTV = (TextView)findViewById(R.id.MessageDC_CopyTV);

        WholeMDC = (LinearLayout)findViewById(R.id.wholeMDC);



        Intent intent = getIntent();


        friendId = intent.getExtras().getString("friendId");
        no = intent.getExtras().getInt("no");
        time = intent.getExtras().getLong("time");
        position = intent.getExtras().getInt("position");
        subType = intent.getExtras().getString("subType");
        content = intent.getExtras().getString("content");


        if(!subType.equals("text")){

            copyTV.setVisibility(View.GONE);
            WholeMDC.getLayoutParams().height = (int) (100 * Resources.getSystem().getDisplayMetrics().density);

        }


        db = new MyDatabaseOpenHelper(this,"catchMind",null,1);

        deleteTV.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                db.deleteMessageData(no,friendId,time);

                Intent resultIntent = new Intent();

                resultIntent.putExtra("position",position);
                resultIntent.putExtra("type","del");

                setResult(RESULT_OK,resultIntent);

                finish();
            }
        });


        copyTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Intent resultIntent = new Intent();

                resultIntent.putExtra("position",position);
                resultIntent.putExtra("type","copy");
                resultIntent.putExtra("subType",subType);
                resultIntent.putExtra("content",content);

                setResult(RESULT_OK,resultIntent);

                finish();

            }
        });

    }


}
