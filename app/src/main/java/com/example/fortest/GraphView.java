package com.example.fortest;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.List;

public class GraphView extends View {

    private Paint paint;
    private List<Point> coordinates;
    private float xMin, xMax, yMin, yMax;

    public GraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(1);
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setTextSize(10);
        paint.setTextAlign(Align.CENTER);
        setAxisRange(-4,4,-4,4);
    }
    public void setCoordinates(List<Point> coordinates) {
        this.coordinates = coordinates;
        invalidate();
    }
    public void setAxisRange(float xMin, float xMax, float yMin, float yMax) {
        this.xMin = xMin;
        this.xMax = xMax;
        this.yMin = yMin;
        this.yMax = yMax;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float centerX = getWidth() / 2;
        float centerY = getHeight() / 2;

        float xRange = xMax - xMin;
        float yRange = yMax - yMin;

        float axisLength = Math.min(getWidth(), getHeight()) * 0.8f / 2;

        // 绘制坐标轴
        canvas.drawLine(centerX - axisLength, centerY, centerX + axisLength, centerY, paint);
        canvas.drawLine(centerX, centerY - axisLength, centerX, centerY + axisLength, paint);

        // 绘制 x 轴上的数字
        Log.i("YCJ", " centerX = " + centerX + " centerY = " + centerY + " axisLength = " + axisLength);
        for (float i = xMin; i <= xMax; i++) {
            float x = centerX + i * axisLength / (xRange/2);
            float y = centerY;
            canvas.drawText(String.valueOf(i), x, centerY + 20, paint);
        }

        Log.i("YCJ", "ymin = " + yMin + " yMax = " + yMax);
        // 绘制 y 轴上的数字
        for (float i = yMin; i <= yMax; i++) {
            if (i == 0) {
                continue;
            }
            float x = centerX;
            float y = centerY - i * axisLength /  (yRange/2);
            canvas.drawText(String.valueOf(i), centerX - 10, y, paint);
        }

        // 绘制坐标点
        if (coordinates != null) {
            for (Point point : coordinates) {
                float x = centerX + point.x * axisLength / (xRange/2);
                float y = centerY - point.y * axisLength / (yRange/2);
                paint.setStyle(Paint.Style.FILL);  // 设置画笔样式为实心
                canvas.drawCircle(x, y, 5, paint);
                // 显示坐标值
                canvas.drawText("(" + point.x + "," + point.y + ")", x, y - 20, paint);
            }
        }
    }

    public static class Point {
        public float x;
        public float y;

        public Point(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }
}
