package com.example.el_mozote.reservations;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.example.el_mozote.R;

import java.util.Locale;

public class OdometerGaugeView extends View {
    private static final float START_ANGLE = 140f;
    private static final float SWEEP_ANGLE = 260f;

    private float minimum;
    private float maximum;
    private float step;
    private float value;
    private boolean fuelMode;
    private OnValueChangeListener listener;

    private final Paint arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint scalePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint needlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pivotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public interface OnValueChangeListener {
        void onValueChanged(float value);
    }

    public OdometerGaugeView(Context context) {
        super(context);
        init(context, null);
    }

    public OdometerGaugeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public OdometerGaugeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        minimum = 0f;
        maximum = 300000f;
        step = 10f;
        value = minimum;

        if (attrs != null) {
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.OdometerGaugeView);
            minimum = array.getFloat(R.styleable.OdometerGaugeView_odometerMin, minimum);
            maximum = array.getFloat(R.styleable.OdometerGaugeView_odometerMax, maximum);
            step = array.getFloat(R.styleable.OdometerGaugeView_odometerStep, step);
            fuelMode = array.getBoolean(R.styleable.OdometerGaugeView_odometerFuelMode, false);
            array.recycle();
        }

        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setStrokeCap(Paint.Cap.ROUND);
        arcPaint.setColor(Color.parseColor("#E2E8F0"));

        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        progressPaint.setColor(Color.parseColor("#10B981"));

        scalePaint.setStrokeWidth(3f);
        scalePaint.setColor(Color.parseColor("#64748B"));

        needlePaint.setStrokeWidth(5f);
        needlePaint.setStrokeCap(Paint.Cap.ROUND);
        needlePaint.setColor(Color.parseColor("#334155"));

        pivotPaint.setColor(Color.parseColor("#334155"));

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.MONOSPACE);
        textPaint.setColor(Color.parseColor("#334155"));
        textPaint.setStrokeWidth(1f);
    }

    public void setRange(float minimum, float maximum, float step) {
        this.minimum = minimum;
        this.maximum = maximum;
        this.step = step;
        setValue(minimum, false);
    }

    public void setFuelMode(boolean fuelMode) {
        this.fuelMode = fuelMode;
        if (fuelMode) {
            setRange(0f, 4f, 1f);
            setValue(4f, false);
        }
    }

    public void setOnValueChangeListener(OnValueChangeListener listener) {
        this.listener = listener;
    }

    public float getGaugeValue() {
        return value;
    }

    public void setValue(float newValue, boolean animate) {
        float clamped = Math.max(minimum, Math.min(maximum, snap(newValue)));
        if (!animate) {
            value = clamped;
            invalidate();
            if (listener != null) listener.onValueChanged(value);
            return;
        }

        ValueAnimator animator = ValueAnimator.ofFloat(value, clamped);
        animator.setDuration(220);
        animator.addUpdateListener(animation -> {
            value = (float) animation.getAnimatedValue();
            invalidate();
            if (listener != null) listener.onValueChanged(value);
        });
        animator.start();
    }

    private float snap(float rawValue) {
        if (maximum <= minimum) return minimum;
        float snapped = Math.round((rawValue - minimum) / step) * step + minimum;
        return Math.max(minimum, Math.min(maximum, snapped));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int size = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (size == 0) size = height;
        if (size == 0) size = 360;
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float cx = getWidth() / 2f;
        float cy = getHeight() * 0.56f;
        float radius = Math.min(getWidth(), getHeight()) * 0.36f;
        RectF arcRect = new RectF(cx - radius, cy - radius, cx + radius, cy + radius);

        float stroke = radius * 0.11f;
        arcPaint.setStrokeWidth(stroke);
        progressPaint.setStrokeWidth(stroke);
        canvas.drawArc(arcRect, START_ANGLE, SWEEP_ANGLE, false, arcPaint);

        float ratio = maximum == minimum ? 0f : (value - minimum) / (maximum - minimum);
        float progressSweep = SWEEP_ANGLE * ratio;
        progressPaint.setColor(progressColor(ratio));
        canvas.drawArc(arcRect, START_ANGLE, progressSweep, false, progressPaint);

        float scaleInner = radius - stroke * 1.15f;
        for (int i = 0; i <= 26; i++) {
            double angle = Math.toRadians(START_ANGLE + SWEEP_ANGLE * i / 26f);
            float outer = radius - stroke * 0.45f;
            float inner = i % 5 == 0 ? scaleInner - radius * 0.12f : scaleInner;
            float x1 = cx + (float) Math.cos(angle) * inner;
            float y1 = cy + (float) Math.sin(angle) * inner;
            float x2 = cx + (float) Math.cos(angle) * outer;
            float y2 = cy + (float) Math.sin(angle) * outer;
            canvas.drawLine(x1, y1, x2, y2, scalePaint);
        }

        float needleAngle = (float) Math.toRadians(START_ANGLE + progressSweep);
        float needleLength = radius - stroke * 1.9f;
        canvas.drawLine(cx, cy,
                cx + (float) Math.cos(needleAngle) * needleLength,
                cy + (float) Math.sin(needleAngle) * needleLength,
                needlePaint);

        pivotPaint.setColor(Color.parseColor("#334155"));
        canvas.drawCircle(cx, cy, radius * 0.08f, pivotPaint);

        textPaint.setTextSize(radius * 0.18f);
        String digitalText = fuelMode
                ? ((int) value) + " / 4"
                : String.format(Locale.US, "%,.0f km", value);
        canvas.drawText(digitalText, cx, cy + radius * 0.42f, textPaint);
    }

    private int progressColor(float ratio) {
        if (ratio < 0.75f) return Color.parseColor("#10B981");
        if (ratio < 0.9f) return Color.parseColor("#F59E0B");
        return Color.parseColor("#EF4444");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN
                || event.getAction() == MotionEvent.ACTION_MOVE) {
            float cx = getWidth() / 2f;
            float cy = getHeight() * 0.56f;

            double rawAngle = Math.toDegrees(Math.atan2(event.getY() - cy, event.getX() - cx));
            float normalized = (float) rawAngle;
            if (normalized < 0f) normalized += 360f;
            if (normalized < START_ANGLE) normalized += 360f;
            if (normalized > START_ANGLE + SWEEP_ANGLE) normalized = START_ANGLE + SWEEP_ANGLE;

            float ratio = (normalized - START_ANGLE) / SWEEP_ANGLE;
            float newValue = minimum + (maximum - minimum) * ratio;
            setValue(newValue, true);
            getParent().requestDisallowInterceptTouchEvent(true);
            return true;
        }
        return super.onTouchEvent(event);
    }
}
