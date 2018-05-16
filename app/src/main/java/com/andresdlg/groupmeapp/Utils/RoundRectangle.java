package com.andresdlg.groupmeapp.Utils;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;

import com.takusemba.spotlight.shape.Shape;

public class RoundRectangle implements Shape {

    private float width;
    private float height;
    private float x;
    private float y;

    public RoundRectangle(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public void draw(Canvas canvas, PointF point, float value, Paint paint) {
        canvas.drawRect(x,y+18,(int)width,y+height,paint);
    }

    @Override
    public int getHeight() {
        return 0;
    }

    @Override
    public int getWidth() {
        return 0;
    }

}