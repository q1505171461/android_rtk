package com.example.fortest;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.math.BigDecimal;
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
        paint.setTextSize(16);
        paint.setTextAlign(Align.CENTER);
        setAxisRange(-0.06,0.06,-0.06,0.06);
    }
    public void setCoordinates(List<Point> coordinates) {
        this.coordinates = coordinates;
        invalidate();
    }
    public void setAxisRange(double xMin, double xMax, double yMin, double yMax) {
        this.xMin = (float) xMin;
        this.xMax = (float) xMax;
        this.yMin = (float) yMin;
        this.yMax = (float) yMax;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float centerX = getWidth() / 2;
        float centerY = getHeight() / 2;

        float axisLength = Math.min(getWidth(), getHeight()) * 0.8f / 2;

        // 绘制坐标轴
        canvas.drawLine(centerX - axisLength, centerY, centerX + axisLength, centerY, paint);
        canvas.drawLine(centerX, centerY - axisLength, centerX, centerY + axisLength, paint);

        // 绘制 x 轴上的数字
//        for (float i = xMin; i <= xMax; i++) {
//            float x = centerX + (i - XCenter) * axisLength / xRange;
//            float y = centerY;
//            canvas.drawText(String.valueOf(i), x, centerY + 20, paint);
//        }

        for (int i = 0; i < 7; i++) {
//            X轴上的坐标
            float x = centerX + (i - 3)  * axisLength / 3;
            float y = centerY;
            double xValue = xMin + ((xMax - xMin) / 6) * i;
            BigDecimal b = new BigDecimal(xValue);
            //保留2位小数
            double f1 = b.setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
            String stringValue1 = String.valueOf(f1).replaceAll("(\\.[1-9]*)0*$", "$1");  // 删除末尾的零
            System.out.printf("\n11111111111%s %s\n", stringValue1, String.valueOf(f1));
            if (!(i==1 || i == 2 || i == 4 || i == 5)){
                canvas.drawText(stringValue1, x, centerY + 20, paint);
            }

            canvas.drawLine(x,y,x,y-10,paint);
//            Y轴上的坐标
            if (i == 3) {
                continue;
            }
            float x1 = centerX;
            float y1 = centerY - (i - 3)  * axisLength / 3;
            double yValue = yMin + ((yMax - yMin) / 6) * i;
            BigDecimal b1 = new BigDecimal(yValue);

            //保留2位小数
            double f2 = b1.setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
            String stringValue2 = String.valueOf(f2).replaceAll("(\\.\\d*[1-9])0*$", "$1");  // 删除末尾的零
//            System.out.printf("\n11111111111%s\n", stringValue2);
            canvas.drawLine(x1,y1,x1+10,y1,paint);
            if (!(i==1 || i == 2 || i == 4 || i == 5)){
                canvas.drawText(stringValue2, centerX - 14, y1, paint);
            }

        }

        // 绘制 y 轴上的数字
//        for (float i = yMin; i <= yMax; i++) {
//            if (i == YCenter) {
//                continue;
//            }
//            float y = centerY - (i - YCenter) * axisLength / yRange;
//            float x = centerX;
//            canvas.drawText(String.valueOf(i), centerX - 10, y, paint);
//        }
//
        // 绘制坐标点

        float XCenter = (xMin + xMax) /2;
        float YCenter = (yMin + yMax) /2;
        float xRange = (xMax - xMin)/2;
        float yRange = (yMax - yMin)/2;

        if (coordinates != null) {
            for (Point point : coordinates) {
                float x = centerX + (point.x - XCenter) * axisLength / xRange;
                float y = centerY - (point.y - YCenter) * axisLength / yRange;
                paint.setStyle(Paint.Style.FILL);  // 设置画笔样式为实心
                canvas.drawCircle(x, y, 3, paint);
                // 显示坐标值
//                canvas.drawText("(" + point.x + "," + point.y + ")", x, y - 20, paint);
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
