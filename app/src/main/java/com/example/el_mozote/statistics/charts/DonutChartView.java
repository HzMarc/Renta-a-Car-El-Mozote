package com.example.el_mozote.statistics.charts;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DonutChartView extends View {

    public static class SliceItem {
        public final String label;
        public final float value;
        public final int color;

        public SliceItem(String label, float value, int color) {
            this.label = label;
            this.value = value;
            this.color = color;
        }
    }

    private final List<SliceItem> slices = new ArrayList<>();
    private final Paint arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint centerTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint subTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF oval = new RectF();

    private String centerText = "0%";
    private String centerSubtext = "Utilización";

    public DonutChartView(Context context) {
        super(context);
        init();
    }

    public DonutChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DonutChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setStrokeCap(Paint.Cap.ROUND);

        centerTextPaint.setTextAlign(Paint.Align.CENTER);
        centerTextPaint.setColor(Color.parseColor("#1E293B"));
        centerTextPaint.setTextSize(spToPx(22));
        centerTextPaint.setFakeBoldText(true);

        subTextPaint.setTextAlign(Paint.Align.CENTER);
        subTextPaint.setColor(Color.parseColor("#64748B"));
        subTextPaint.setTextSize(spToPx(11));
    }

    public void setData(List<SliceItem> data, String centerText, String centerSubtext) {
        slices.clear();
        if (data != null) slices.addAll(data);
        this.centerText = centerText != null ? centerText : "0%";
        this.centerSubtext = centerSubtext != null ? centerSubtext : "Utilización";
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        if (width <= 0 || height <= 0) return;

        float strokeWidth = dpToPx(18);
        arcPaint.setStrokeWidth(strokeWidth);

        float size = Math.min(width, height) - strokeWidth * 2;
        float left = (width - size) / 2f;
        float top = (height - size) / 2f;
        oval.set(left, top, left + size, top + size);

        float total = 0f;
        for (SliceItem item : slices) {
            total += item.value;
        }

        if (total <= 0f) {
            // Draw gray placeholder ring
            arcPaint.setColor(Color.parseColor("#E2E8F0"));
            canvas.drawArc(oval, 0f, 360f, false, arcPaint);
        } else {
            float startAngle = -90f;
            for (SliceItem item : slices) {
                if (item.value <= 0) continue;
                float sweepAngle = (item.value / total) * 360f;
                arcPaint.setColor(item.color);
                canvas.drawArc(oval, startAngle + 2f, sweepAngle - 4f, false, arcPaint);
                startAngle += sweepAngle;
            }
        }

        // Draw central bold metric
        float centerX = width / 2f;
        float centerY = height / 2f;

        canvas.drawText(centerText, centerX, centerY + dpToPx(2), centerTextPaint);
        canvas.drawText(centerSubtext, centerX, centerY + dpToPx(20), subTextPaint);
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    private float spToPx(float sp) {
        return sp * getResources().getDisplayMetrics().scaledDensity;
    }
}
