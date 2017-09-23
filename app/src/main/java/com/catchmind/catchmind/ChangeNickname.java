package com.catchmind.catchmind;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

/**
 * Created by sonsch94 on 2017-09-24.
 */

public class ChangeNickname extends Activity {

    public EditText EditNickname;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_change_nickname);
        EditNickname = (EditText)findViewById(R.id.editNickname);
        Intent intent = getIntent();
        EditNickname.setText(intent.getExtras().getString("nickname"));
    }


    public void okChange(View v){

        Intent resultIntent = new Intent();

        resultIntent.putExtra("nickname",EditNickname.getText().toString());

        setResult(RESULT_OK,resultIntent);

        finish();

    }


    public void cancelChange(View v){

        finish();

    }

}
