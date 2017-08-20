package com.catchmind.catchmind;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by sonsch94 on 2017-08-21.
 */

public class ChatRoomViewPager extends ViewPager {

    public static boolean DrawMode = false;
    public ChatRoomViewPager(Context context) {
        super(context);
    }
    public ChatRoomViewPager(Context context, AttributeSet attrs){
        super(context,attrs);
    }
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return DrawMode ? false : super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return DrawMode ? false : super.onTouchEvent(event);
    }

}
