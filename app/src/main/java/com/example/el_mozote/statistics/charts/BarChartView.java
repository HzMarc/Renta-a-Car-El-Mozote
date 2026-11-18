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
import java.util.Locale;

public class BarChartView extends View {

    public static class BarItem {
        public final String label;
        public final double value;
        public final String formattedValue;
        public final int color;

        public BarItem(String label, double value, String formattedValue, int color) {
            this.label = label;
            this.value = value;
            this.formattedValue = formattedValue;
            this.color = color;
        }
    }

    private final List<BarItem> items = new ArrayList<>();
    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF barRect = new RectF();

    public BarChartView(Context context) {
        super(context);
        init();
    }

    public BarChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BarChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(Color.parseColor("#475569"));
        textPaint.setTextSize(spToPx(10));

        gridPaint.setColor(Color.parseColor("#E2E8F0"));
        gridPaint.setStrokeWidth(dpToPx(1));
    }

    public void setData(List<BarItem> data) {
        items.clear();
        if (data != null) items.addAll(data);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        if (width <= 0 || height <= 0) return;

        float paddingLeft = dpToPx(16);
        float paddingRight = dpToPx(16);
        float paddingTop = dpToPx(24);
        float paddingBottom = dpToPx(32);

        float chartWidth = width - paddingLeft - paddingRight;
        float chartHeight = height - paddingTop - paddingBottom;
        float baselineY = height - paddingBottom;

        // Baseline
        canvas.drawLine(paddingLeft, baselineY, width - paddingRight, baselineY, gridPaint);

        if (items.isEmpty()) {
            textPaint.setColor(Color.parseColor("#94A3B8"));
            textPaint.setTextSize(spToPx(12));
            canvas.drawText("Sin datos para graficar en este período", width / 2f, height / 2f, textPaint);
            return;
        }

        // Find maximum value
        double maxVal = 0.0;
        for (BarItem item : items) {
            if (item.value > maxVal) maxVal = item.value;
        }
        if (maxVal <= 0.0) maxVal = 1.0;

        int count = items.size();
        float barSlotWidth = chartWidth / count;
        float barWidth = Math.min(dpToPx(36), barSlotWidth * 0.6f);
        float cornerRadius = dpToPx(6);

        for (int i = 0; i < count; i++) {
            BarItem item = items.get(i);
            float centerX = paddingLeft + (i * barSlotWidth) + (barSlotWidth / 2f);
            float barHeight = (float) ((item.value / maxVal) * (chartHeight * 0.85f));
            if (item.value > 0 && barHeight < dpToPx(6)) barHeight = dpToPx(6);

            float topY = baselineY - barHeight;
            float left = centerX - (barWidth / 2f);
            float right = centerX + (barWidth / 2f);

            barRect.set(left, topY, right, baselineY);
            barPaint.setColor(item.color);
            canvas.drawRoundRect(barRect, cornerRadius, cornerRadius, barPaint);

            // Draw Value above bar
            textPaint.setColor(Color.parseColor("#1E293B"));
            textPaint.setTextSize(spToPx(10));
            textPaint.setFakeBoldText(true);
            canvas.drawText(item.formattedValue, centerX, topY - dpToPx(6), textPaint);

            // Draw Label below baseline
            textPaint.setColor(Color.parseColor("#64748B"));
            textPaint.setTextSize(spToPx(10));
            textPaint.setFakeBoldText(false);
            String displayLabel = item.label;
            if (displayLabel.length() > 10) {
                displayLabel = displayLabel.substring(0, 8) + "…";
            }
            canvas.drawText(displayLabel, centerX, baselineY + dpToPx(18), textPaint);
        }
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    private float spToPx(float sp) {
        return sp * getResources().getDisplayMetrics().scaledDensity;
    }
}
