package com.catchmind.catchmind;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class DrawLine extends View
{
    //현재 그리기 조건(색상, 굵기, 등등.)을 기억 하는 변수.
    private Paint paint = null;

    //그리기를 할 bitmap 객체. -- 도화지라고 생각하면됨.
    private Bitmap  bitmap = null;
    private Bitmap resizedBitmap = null;

    //bitmap 객체의 canvas 객체. 실제로 그리기를 하기 위한 객체.. -- 붓이라고 생각하면됨.
    private Canvas  canvas = null;

    //마우스 포인터(손가락)이 이동하는 경로 객체.
    private Path    path;
    private Path   Rpath;

    //마우스 포인터(손가락)이 가장 마지막에 위치한 x좌표값 기억용 변수.
    private float   oldX;

    //마우스 포인터(손가락)이 가장 마지막에 위치한 y좌표값 기억용 변수.
    private float   oldY;

    public ChatRoomActivity cra;

    sendToActivity STA;

    public ArrayList<Coordinate> pathList = new ArrayList<>();

    public int OriginalWidth;

    public int ResizeWidth;

    public Context mContext;

    /**
     * 생성자.. new DrawLine(this, rect) 하면 여기가 호출됨.
     * @param context   Context객체
     * @param rect      그리기 범위 화면 사이즈
     */
    public DrawLine(Context context, Rect rect,ChatRoomActivity CRA,int originalWidth)
    {
        this(context);
        this.mContext = context;

        cra = CRA;
        STA = (sendToActivity) cra;

        //그리기를 할 bitmap 객체 생성.
        bitmap = Bitmap.createBitmap(rect.width(), rect.height(),
                Bitmap.Config.ARGB_8888);
        //그리기 bitmap에서 canvas를 알아옴.
        canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);
        //경로 초기화.
        path = new Path();
        Rpath = new Path();

        this.OriginalWidth = originalWidth;

        setLineColor();
    }

    @Override
    protected void onDetachedFromWindow()
    {
        //앱 종료시 그리기 bitmap 초기화 시킴...
        if(bitmap!= null) bitmap.recycle();
        bitmap = null;

        super.onDetachedFromWindow();
    }

    @Override
    public void onDraw(Canvas canvas)
    {
        //그리기 bitmap이 있으면 현재 화면에 bitmap을 그린다.
        //자바의 view는 onDraw할때 마다 화면을 싹 지우고 다시 그리게 됨.
        if(resizedBitmap != null)
        {
            int Half = (OriginalWidth - ResizeWidth)/2;

            canvas.drawBitmap(resizedBitmap, Half, 0, null);
        }else if( bitmap != null){
            canvas.drawBitmap(bitmap, 0, 0, null);
        }

    }



    //이벤트 처리용 함수..
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if(!ChatRoomViewPager.DrawMode){
            return false;
        }

        float x = event.getX();
        float y = event.getY();

        Log.d("담배",x+"###"+y);

        switch (event.getAction() & MotionEvent.ACTION_MASK)
        {
            case MotionEvent.ACTION_DOWN:
            {
                Log.d("담배다운",x+"###"+y);
                //최초 마우스를 눌렀을때(손가락을 댓을때) 경로를 초기화 시킨다.
                path.reset();

                //그다음.. 현재 경로로 경로를 이동 시킨다.
                path.moveTo(x, y);

                //포인터 위치값을 기억한다.

                oldX = x;
                oldY = y;

                //계속 이벤트 처리를 하겠다는 의미.
                return true;
            }
            case MotionEvent.ACTION_MOVE:
            {
                Log.d("담배무브",x+"###"+y);
                //포인트가 이동될때 마다 두 좌표(이전에눌렀던 좌표와 현재 이동한 좌료)간의 간격을 구한다.
                float dx = Math.abs(x - oldX);
                float dy = Math.abs(y - oldY);

                //두 좌표간의 간격이 4px이상이면 (가로든, 세로든) 그리기 bitmap에 선을 그린다.
                if (dx >= 1 || dy >= 1)
                {
                    //path에 좌표의 이동 상황을 넣는다. 이전 좌표에서 신규 좌표로..
                    //lineTo를 쓸수 있지만.. 좀더 부드럽게 보이기 위해서 quadTo를 사용함.
                    path.quadTo(oldX, oldY, x, y);

                    JSONArray jarray = new JSONArray();
                    String sendPath = "";
                    try {
                        jarray.put(oldX);
                        jarray.put(oldY);
                        jarray.put(x);
                        jarray.put(y);
                        sendPath = jarray.toString();
                    }catch(JSONException e){
                        e.printStackTrace();
                    }

                    Coordinate tmpCD = new Coordinate(oldX,oldY,x,y);
                    pathList.add(tmpCD);

                    //포인터의 마지막 위치값을 기억한다.
                    oldX = x;
                    oldY = y;

                    //그리기 bitmap에 path를 따라서 선을 그린다.
                    canvas.drawPath(path, paint);


                    STA.sendPath(sendPath);
                }

                //화면을 갱신시킴.. 이 함수가 호출 되면 onDraw 함수가 실행됨.
                invalidate();

                //계속 이벤트 처리를 하겠다는 의미.
                return true;
            }
        }


        //더이상 이벤트 처리를 하지 않겠다는 의미.
        return false;
    }


    public void setLineColor()
    {
        paint = new Paint();
        paint.setColor(Color.BLACK);

        paint.setAlpha(255);
        paint.setDither(true);
        paint.setStrokeWidth(10);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setAntiAlias(true);
    }


    public DrawLine(Context context)
    {
        super(context);
    }

    public void receiveLine(String PATH){
        try {
            Rpath.reset();

            JSONArray jarray = new JSONArray(PATH);

            float a = (float) jarray.getDouble(0);
            float b = (float) jarray.getDouble(1);
            float c = (float) jarray.getDouble(2);
            float d = (float) jarray.getDouble(3);

            Log.d("되냐",a+"####"+b+"####"+c+"####"+d);
            Rpath.moveTo(a,b);
            Rpath.quadTo(a,b,c,d);
            canvas.drawPath(Rpath, paint);
            invalidate();
        }catch (JSONException e){
            e.printStackTrace();
        }

    }

    public void changeBitmap(int width){
        if(width < OriginalWidth) {
            int HalfOW = (int) OriginalWidth / 2;
            int HalfWidth = (int) width / 2;
            ResizeWidth = width;

            resizedBitmap = Bitmap.createBitmap(bitmap, HalfOW - HalfWidth, 0, width, width);

            canvas = new Canvas(resizedBitmap);

            float rate = (float)width/(float)OriginalWidth;

            for(int i=0;i<pathList.size();i++){

                float Oldx = pathList.get(i).oldX;
                float Oldy = pathList.get(i).oldY;
                float x = pathList.get(i).X;
                float y = pathList.get(i).Y;

                Log.d("비트맵1",Oldx+"###"+Oldy+"###"+x+"###"+y+"###");

                float NOldx = Oldx*rate;
                float NOldy = Oldy*rate;
                float Nx = x*rate;
                float Ny = y*rate;

                Log.d("비트맵2",NOldx+"###"+NOldy+"###"+Nx+"###"+Ny+"###");

                path.reset();
                path.moveTo(NOldx,NOldy);
                path.quadTo(NOldx,NOldy,Nx,Ny);
                canvas.drawPath(path,paint);
            }
                invalidate();
            Log.d("디버깅",width+"###"+HalfOW+"###"+HalfWidth);

            invalidate();

        }else{
            canvas = new Canvas(bitmap);
            resizedBitmap = null;
            Log.d("디버깅",width+"###");

            invalidate();

        }

    }

    public interface sendToActivity{
        void sendPath(String PATH);
    }
}
