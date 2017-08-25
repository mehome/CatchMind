package com.catchmind.catchmind;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

/**
 * Created by sonsch94 on 2017-08-25.
 */

public class ColorPickerActivity extends Activity{
    @Override


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_colorpicker);
        this.setFinishOnTouchOutside(false);

    }

    public void colorpicker_back(View v){
        finish();
    }

    public void button_red(View v){
        Intent resultIntent = new Intent();

        resultIntent.putExtra("color","#FF0000");
        resultIntent.putExtra("textColor", "#FFFFFF");

        setResult(RESULT_OK,resultIntent);

        finish();
    }

    public void button_blue(View v){
        Intent resultIntent = new Intent();

        resultIntent.putExtra("color","#0000FF");
        resultIntent.putExtra("textColor", "#FFFFFF");

        setResult(RESULT_OK,resultIntent);

        finish();
    }

    public void button_green(View v){
        Intent resultIntent = new Intent();

        resultIntent.putExtra("color","#00FF00");
        resultIntent.putExtra("textColor", "#FFFFFF");

        setResult(RESULT_OK,resultIntent);

        finish();
    }

    public void button_yellow(View v){
        Intent resultIntent = new Intent();

        resultIntent.putExtra("color","#FFFF00");
        resultIntent.putExtra("textColor", "#000000");

        setResult(RESULT_OK,resultIntent);

        finish();
    }


    public void button_black(View v){
        Intent resultIntent = new Intent();

        resultIntent.putExtra("color","#000000");
        resultIntent.putExtra("textColor", "#FFFFFF");

        setResult(RESULT_OK,resultIntent);

        finish();
    }


    public void button_orange(View v){
        Intent resultIntent = new Intent();

        resultIntent.putExtra("color","#FFA500");
        resultIntent.putExtra("textColor", "#FFFFFF");

        setResult(RESULT_OK,resultIntent);

        finish();
    }


    public void button_purple(View v){
        Intent resultIntent = new Intent();

        resultIntent.putExtra("color","#800080");
        resultIntent.putExtra("textColor", "#FFFFFF");

        setResult(RESULT_OK,resultIntent);

        finish();
    }


    public void button_brown(View v){
        Intent resultIntent = new Intent();

        resultIntent.putExtra("color","#A52A2A");
        resultIntent.putExtra("textColor", "#FFFFFF");


        setResult(RESULT_OK,resultIntent);

        finish();
    }

    public void button_hotpink(View v){
        Intent resultIntent = new Intent();

        resultIntent.putExtra("color","#FF69B4");
        resultIntent.putExtra("textColor", "#FFFFFF");

        setResult(RESULT_OK,resultIntent);

        finish();
    }


    public void button_white(View v){
        Intent resultIntent = new Intent();

        resultIntent.putExtra("color","#FFFFFF");
        resultIntent.putExtra("textColor", "#000000");
        setResult(RESULT_OK,resultIntent);

        finish();
    }

}