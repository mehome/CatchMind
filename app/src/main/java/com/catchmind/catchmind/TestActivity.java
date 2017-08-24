package com.catchmind.catchmind;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;

/**
 * Created by sonsch94 on 2017-08-24.
 */

public class TestActivity extends AppCompatActivity {

    ImageView IV;
    TextView TV;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);


        IV = (ImageView) findViewById(R.id.youtubeImage);
        TV = (TextView) findViewById(R.id.youtubeText);



        Intent youtubeIntent = getIntent();
        Bundle extras = getIntent().getExtras();
        if(extras!=null) {
            String link = extras.getString(Intent.EXTRA_TEXT);
            TV.setText(link);
            String forId[] = link.split("/");
            String videoId = forId[3];

            Glide.with(this).load("http://img.youtube.com/vi/"+videoId+"/default.jpg")
                    .error(R.drawable.default_profile_image)
                    .into(IV);
        }

        Log.d("유튜브",youtubeIntent.toString());

    }
}
