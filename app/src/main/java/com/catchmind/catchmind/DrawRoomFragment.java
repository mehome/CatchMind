package com.catchmind.catchmind;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

/**
 * Created by sonsch94 on 2017-07-19.
 */

public class DrawRoomFragment extends Fragment implements ChatRoomActivity.DrawCommunicator{

    RelativeLayout sketchBook;
    LinearLayout widthContainer;
    View widthState;
    TextView colorPickerBtn;
    ImageButton clearBtn;

    private DrawLine drawLine = null;
    ChatRoomActivity cra;
    int width = 0;
    int height = 0;
    WidthView WV ;

    final static int colorRequest = 9876;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.draw_room_fragment, container, false);


        cra = (ChatRoomActivity)getActivity();

        sketchBook = (RelativeLayout) rootView.findViewById(R.id.SketchBook);
        widthContainer = (LinearLayout) rootView.findViewById(R.id.widthContainer);
        colorPickerBtn = (TextView) rootView.findViewById(R.id.colorPickerBtn);
        clearBtn = (ImageButton) rootView.findViewById(R.id.clearBtn);

        colorPickerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent colorIntent = new Intent(getContext(),ColorPickerActivity.class);
                startActivityForResult(colorIntent,colorRequest);
            }
        });

        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(drawLine != null){
                    drawLine.clearSketch();
                }
            }
        });

        colorPickerBtn.setBackgroundColor(Color.BLACK);


        return rootView;

    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ViewTreeObserver vto = sketchBook.getViewTreeObserver();

        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                width  = sketchBook.getWidth();
                height = sketchBook.getHeight();
                sketchBook.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                Log.d("체크사이즈OAC",width+"###"+height);
                if(sketchBook != null) //그리기 뷰가 보여질 레이아웃이 있으면...
                {


                    //그리기 뷰 레이아웃의 넓이와 높이를 찾아서 Rect 변수 생성.
                    Rect rect = new Rect(0, 0, width, height);

                    //그리기 뷰 초기화..
                    drawLine = new DrawLine(getContext(), rect, cra );

                    //그리기 뷰를 그리기 뷰 레이아웃에 넣기 -- 이렇게 하면 그리기 뷰가 화면에 보여지게 됨.
                    sketchBook.addView(drawLine);

                    float lineLength = width * 100 / 1080;

                    widthContainer.getLayoutParams().width = ((int)lineLength);

                    WV = new WidthView(getContext(),(float)width);

                    widthContainer.addView(WV);

                }

            }
        });



    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == colorRequest){
            if(resultCode == RESULT_OK){
                String color = data.getExtras().getString("color");
                String textColor = data.getExtras().getString("textColor");
                colorPickerBtn.setBackgroundColor(Color.parseColor(color));
                drawLine.setPaintColor(Color.parseColor(color));
                colorPickerBtn.setTextColor(Color.parseColor(textColor));
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();


    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void receivePath(String PATH) {
        drawLine.receiveLine(PATH);
    }

    @Override
    public void receiveClear() {
        drawLine.receiveClearSketch();
    }

    @Override
    public void resizeSketchBook() {
        try {
            ViewTreeObserver vto = sketchBook.getViewTreeObserver();

            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {

                    width = sketchBook.getWidth();
                    height = sketchBook.getHeight();
                    sketchBook.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    Log.d("체크사이즈", width + "###" + height);
                    drawLine.changeBitmap(height);
                }
            });
        }catch (NullPointerException e){
            e.printStackTrace();
        }
    }


    @Override
    public void MinusWidth() {
        if(drawLine.MinusLineWidth()){
            WV.MinusLineWidth();
        }
    }

    @Override
    public void PlusWidth() {
        if(drawLine.PlusLineWidth()){
            WV.PlusLineWidth();
        }
    }
}
