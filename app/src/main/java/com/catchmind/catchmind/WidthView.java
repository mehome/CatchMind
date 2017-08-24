package com.catchmind.catchmind;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.view.View;

/**
 * Created by sonsch94 on 2017-08-24.
 */

public class WidthView extends View {

    public float DefaultWidth;

    private Paint paint = null;

    private Bitmap bitmap = null;

    private Canvas canvas = null;

    private Path path = null;


    public WidthView(Context context)
    {
        super(context);
    }

    public WidthView(Context context, float DW){
        super(context);
        this.DefaultWidth = DW ;
        float startWidth = DW * 10 / 1080;

        Rect rect = new Rect(0, 0, 150, 60);

        bitmap = Bitmap.createBitmap(rect.width(), rect.height(),
                Bitmap.Config.ARGB_8888);


        paint = new Paint();

        paint.setColor(Color.BLACK);

        paint.setAlpha(255);
        paint.setDither(true);
        paint.setStrokeWidth(startWidth);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setAntiAlias(true);

        canvas = new Canvas(bitmap);
        canvas.drawColor(Color.YELLOW);

        path = new Path();
        path.moveTo(5,25);
        path.quadTo(5,25,75,25);
        canvas.drawPath(path,paint);
        invalidate();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(bitmap, 0, 0, null);
    }
}
