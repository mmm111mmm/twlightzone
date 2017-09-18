package com.example.user.fishsgraph;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class MonthGraph extends View {

    private static final int DEFAULT_FONT_SIZE_DP = 9;
    private int[] values = null;
    private int[] dayCoords = null;
    private String[] dayLabels = null;

    private Paint pastPaint;
    private Paint todayPaint;
    private Paint futurePaint;
    private Paint labelPaint;

    private float graphWidth;
    private float dayWidth;
    private float gapWidth;

    int paddingPixels;

    int pastColor;
    int todayColor;
    int futureColor;

    int todayIndex = 15;

    long maxSpend = 0;

    Path[] paths;

    private int fontSize = 0;
    private boolean drawDayLabels = true;
    private boolean daysInitialised = false;

    private Date startDate = null;

    private boolean isThisMonth = true;

    public MonthGraph(Context context) {
        super(context);
        init(context);
    }

    public MonthGraph(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MonthGraph(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context){
        pastColor = Color.parseColor("#339eb2");
        todayColor = Color.parseColor("#ffffff");
        futureColor = Color.parseColor("#545a8c");

        pastPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pastPaint.setStyle(Paint.Style.STROKE);
        pastPaint.setStrokeCap(Paint.Cap.ROUND);
        pastPaint.setColor(pastColor);

        todayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        todayPaint.setStyle(Paint.Style.STROKE);
        todayPaint.setStrokeCap(Paint.Cap.ROUND);
        todayPaint.setColor(todayColor);

        futurePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        futurePaint.setStyle(Paint.Style.STROKE);
        futurePaint.setStrokeCap(Paint.Cap.ROUND);
        futurePaint.setColor(futureColor);

        labelPaint = new Paint(Paint.LINEAR_TEXT_FLAG);
        labelPaint.setColor(Color.parseColor("#66ffffff"));
        fontSize = (int)convertDpToPixel(DEFAULT_FONT_SIZE_DP, context);
        labelPaint.setTextSize(fontSize);

        graphWidth = screenWidth(context);
    }

    private float convertDpToPixel(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }

    public void setValues(List<Integer> valuesList) {
        if(valuesList == null){
            return;
        }
        int[] values = new int[valuesList.size()];
        Iterator<Integer> iterator = valuesList.iterator();
        for (int i = 0; i < values.length; i++) {
            values[i] = Math.abs(iterator.next());
        }
        setValues(values);
    }

    public void setValues(int[] values){
        if(values == null){
            return;
        }
        this.values = values;

        paths = new Path[values.length];
        maxSpend = 0;

        for(int i = 0 ; i < values.length ; i++){
            values[i] = Math.abs(values[i]);
            if(values[i] > maxSpend){
                maxSpend = values[i];
            }
        }

        log("Max spend: " + maxSpend);

        float spaceForDays = (graphWidth/4) * 3;

        dayWidth = spaceForDays / values.length;
        gapWidth = (graphWidth - spaceForDays) / values.length;

        pastPaint.setStrokeWidth(dayWidth);
        todayPaint.setStrokeWidth(dayWidth);
        futurePaint.setStrokeWidth(dayWidth);

        calculatePaths();

        if(drawDayLabels) {
            calculateDays();
        }
    }

    private void calculatePaths(){
        if(values == null){
            return;
        }
        float x = paddingPixels + dayWidth/2 + gapWidth/2;
        float y;

        int availableHeight = getHeight() - (paddingPixels * 2);
        int height = getHeight() - paddingPixels;

        if(drawDayLabels){
            availableHeight = availableHeight - fontSize;
            height = height - fontSize;
        }

        for(int i = 0 ; i < values.length ; i++){
            Path aPath = new Path();
            if(values[i] == 0){
                paths[i] = null;
            }else{
                y = map(values[i], 0, maxSpend, 0, availableHeight);
                aPath.moveTo(x, height);
                aPath.lineTo(x, height - y);
                paths[i] = aPath;
            }


            x = x + dayWidth + gapWidth;
        }
    }

    public void setHorizontalPadding(Context context, int dimenRes){
        if(dimenRes == -1){
            paddingPixels = 0;
        }else{
            paddingPixels = context.getResources().getDimensionPixelOffset(dimenRes);
        }

        int screenWidth = screenWidth(context);
        graphWidth = screenWidth - (paddingPixels * 2);
    }

    private int screenWidth(Context context){
        Configuration configuration = context.getResources().getConfiguration();
        int screenWidthDp = configuration.screenWidthDp;
        return (int) (screenWidthDp * Resources.getSystem().getDisplayMetrics().density);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(values == null){
            return;
        }

        for(int i = 0 ; i < values.length ; i++) {

            if(paths[i] != null) {
                if (i < todayIndex) {
                    canvas.drawPath(paths[i], pastPaint);
                } else if (i == todayIndex) {
                    canvas.drawPath(paths[i], todayPaint);
                } else if (i > todayIndex) {
                    canvas.drawPath(paths[i], futurePaint);
                }
            }

            if(drawDayLabels && daysInitialised && i % 2 == 0) {
                canvas.drawText(dayLabels[i], dayCoords[i], getHeight() - fontSize, labelPaint);
            }
        }
    }

    private float map(float value, float aStart, float aEnd, float bStart, float bEnd) {
        return bStart + (bEnd - bStart) * ((value - aStart) / (aEnd - aStart));
    }

    private void log(String log){
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;

        todayIndex = daysBetween(startDate, new Date());
        log("today index set to: " + todayIndex);

        if(drawDayLabels) {
            calculateDays();
        }
    }

    private void calculateDays(){
        if(values == null || startDate == null){
            return;
        }

        drawDayLabels = true;

        dayCoords = new int[values.length];
        dayLabels = new String[values.length];

        float x = (paddingPixels + dayWidth/2 + gapWidth/2) - dayWidth/2;

        Calendar cal = flatten(startDate);

        int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);

        if(dayOfMonth == 1) {
            for (int i = 0; i < values.length; i++) {
                dayCoords[i] = (int) x;
                dayLabels[i] = String.valueOf(i + 1);
                x = x + dayWidth + gapWidth;
            }
        }else{
            for (int i = 0; i < values.length; i++) {
                dayCoords[i] = (int) x;
                dayLabels[i] = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
                cal.add(Calendar.DATE, 1);
                x = x + dayWidth + gapWidth;
            }
        }

        daysInitialised = true;
    }

    private int daysBetween(Date startDate, Date endDate) {
        Calendar startCalendar = flatten(startDate);
        Calendar endCalendar = flatten(endDate);

        int daysBetween = 0;
        while (startCalendar.before(endCalendar)) {
            startCalendar.add(Calendar.DAY_OF_MONTH, 1);
            daysBetween++;
        }
        return daysBetween;
    }

    private Calendar flatten(Date date){
        Calendar flattened = Calendar.getInstance();
        flattened.setTime(date);
        flattened.set(Calendar.HOUR_OF_DAY, 0);
        flattened.set(Calendar.MINUTE, 0);
        flattened.set(Calendar.SECOND, 0);
        flattened.set(Calendar.MILLISECOND, 1);
        return flattened;
    }
}
