package com.example.el_mozote.statistics.charts;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LineTrendChartView extends View {

    public static class PointItem {
        public final String label;
        public final double value;

        public PointItem(String label, double value) {
            this.label = label;
            this.value = value;
        }
    }

    private final List<PointItem> items = new ArrayList<>();
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pointCenterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Path linePath = new Path();
    private final Path fillPath = new Path();

    public LineTrendChartView(Context context) {
        super(context);
        init();
    }

    public LineTrendChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LineTrendChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        linePaint.setColor(Color.parseColor("#10B981"));
        linePaint.setStrokeWidth(dpToPx(2.5f));
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setStrokeJoin(Paint.Join.ROUND);

        fillPaint.setStyle(Paint.Style.FILL);

        pointPaint.setColor(Color.parseColor("#10B981"));
        pointPaint.setStyle(Paint.Style.FILL);

        pointCenterPaint.setColor(Color.WHITE);
        pointCenterPaint.setStyle(Paint.Style.FILL);

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(Color.parseColor("#64748B"));
        textPaint.setTextSize(spToPx(10));

        gridPaint.setColor(Color.parseColor("#F1F5F9"));
        gridPaint.setStrokeWidth(dpToPx(1));
    }

    public void setData(List<PointItem> data) {
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

        float paddingLeft = dpToPx(20);
        float paddingRight = dpToPx(20);
        float paddingTop = dpToPx(20);
        float paddingBottom = dpToPx(30);

        float chartWidth = width - paddingLeft - paddingRight;
        float chartHeight = height - paddingTop - paddingBottom;
        float baselineY = height - paddingBottom;

        // Draw horizontal grid lines
        canvas.drawLine(paddingLeft, baselineY, width - paddingRight, baselineY, gridPaint);
        canvas.drawLine(paddingLeft, baselineY - chartHeight / 2f, width - paddingRight, baselineY - chartHeight / 2f, gridPaint);

        if (items.isEmpty()) {
            textPaint.setColor(Color.parseColor("#94A3B8"));
            textPaint.setTextSize(spToPx(12));
            canvas.drawText("Sin movimientos registrados en este período", width / 2f, height / 2f, textPaint);
            return;
        }

        double maxVal = 0.0;
        for (PointItem p : items) {
            if (p.value > maxVal) maxVal = p.value;
        }
        if (maxVal <= 0.0) maxVal = 100.0;

        int count = items.size();
        float stepX = (count > 1) ? chartWidth / (count - 1) : chartWidth;

        float[] xCoords = new float[count];
        float[] yCoords = new float[count];

        for (int i = 0; i < count; i++) {
            xCoords[i] = paddingLeft + (i * stepX);
            float ratio = (float) (items.get(i).value / maxVal);
            yCoords[i] = baselineY - (ratio * (chartHeight * 0.8f));
        }

        // Build Paths
        linePath.reset();
        fillPath.reset();

        linePath.moveTo(xCoords[0], yCoords[0]);
        fillPath.moveTo(xCoords[0], baselineY);
        fillPath.lineTo(xCoords[0], yCoords[0]);

        for (int i = 1; i < count; i++) {
            float prevX = xCoords[i - 1];
            float prevY = yCoords[i - 1];
            float currX = xCoords[i];
            float currY = yCoords[i];

            float cPointX1 = prevX + (currX - prevX) / 2f;
            float cPointY1 = prevY;
            float cPointX2 = prevX + (currX - prevX) / 2f;
            float cPointY2 = currY;

            linePath.cubicTo(cPointX1, cPointY1, cPointX2, cPointY2, currX, currY);
            fillPath.cubicTo(cPointX1, cPointY1, cPointX2, cPointY2, currX, currY);
        }

        fillPath.lineTo(xCoords[count - 1], baselineY);
        fillPath.close();

        // Gradient Fill
        fillPaint.setShader(new LinearGradient(
                0, paddingTop, 0, baselineY,
                Color.parseColor("#4410B981"),
                Color.parseColor("#0010B981"),
                Shader.TileMode.CLAMP));

        canvas.drawPath(fillPath, fillPaint);
        canvas.drawPath(linePath, linePaint);

        // Draw points and labels
        for (int i = 0; i < count; i++) {
            float px = xCoords[i];
            float py = yCoords[i];
            PointItem item = items.get(i);

            // Outer circle
            canvas.drawCircle(px, py, dpToPx(4.5f), pointPaint);
            // Inner white circle
            canvas.drawCircle(px, py, dpToPx(2f), pointCenterPaint);

            // Value if nonzero
            if (item.value > 0) {
                textPaint.setColor(Color.parseColor("#065F46"));
                textPaint.setTextSize(spToPx(9));
                textPaint.setFakeBoldText(true);
                canvas.drawText(String.format(Locale.US, "$%.0f", item.value), px, py - dpToPx(8), textPaint);
            }

            // X-Axis Label
            textPaint.setColor(Color.parseColor("#64748B"));
            textPaint.setTextSize(spToPx(10));
            textPaint.setFakeBoldText(false);
            canvas.drawText(item.label, px, baselineY + dpToPx(18), textPaint);
        }
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    private float spToPx(float sp) {
        return sp * getResources().getDisplayMetrics().scaledDensity;
    }
}
