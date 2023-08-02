package com.ashlikun.circleprogress;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Property;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

/**
 * By cindy on 12/22/14 3:53 PM.
 */
public class CircleProgressView extends View {

    private static final Interpolator ANGLE_INTERPOLATOR = new LinearInterpolator();
    private static final Interpolator SWEEP_INTERPOLATOR = new AccelerateDecelerateInterpolator();

    private int angleAnimatorDuration;

    private int sweepAnimatorDuration;

    private int minSweepAngle;

    private float mBorderWidth;

    private final RectF fBounds = new RectF();
    //进度动画
    private ObjectAnimator mObjectAnimatorSweep;
    //View 本身旋转动画
    private ObjectAnimator mObjectAnimatorAngle;
    //是否出现模式
    private boolean mModeAppearing = true;
    private Paint mPaint;
    private float mCurrentGlobalAngleOffset;
    private float mCurrentGlobalAngle;

    private float mCurrentSweepAngle;
    private boolean mRunning;
    private int[] mColors;
    private int mCurrentColorIndex;
    private int mNextColorIndex;

    public CircleProgressView(Context context) {
        this(context, null);
    }

    public CircleProgressView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(
                attrs,
                R.styleable.CircleProgressView,
                defStyleAttr, 0);

        mBorderWidth = a.getDimension(
                R.styleable.CircleProgressView_cpv_borderWidth,
                getResources().getDimension(R.dimen.circle_default_border_width));

        angleAnimatorDuration = a.getInt(
                R.styleable.CircleProgressView_cpv_angleAnimationDurationMillis,
                getResources().getInteger(R.integer.circle_default_angleAnimationDuration));

        sweepAnimatorDuration = a.getInt(
                R.styleable.CircleProgressView_cpv_sweepAnimationDurationMillis,
                getResources().getInteger(R.integer.circle_default_sweepAnimationDuration));

        minSweepAngle = a.getInt(
                R.styleable.CircleProgressView_cpv_minSweepAngle,
                getResources().getInteger(R.integer.circle_default_miniSweepAngle));
        int colorArrayId = -1;
        if (a.hasValue(R.styleable.CircleProgressView_cpv_color)) {
            mColors = new int[1];
            mColors[0] = a.getColor(R.styleable.CircleProgressView_cpv_color, getResources().getColor(R.color.circle_progress_color));
        } else {
            colorArrayId = a.getResourceId(R.styleable.CircleProgressView_cpv_colorSequence,
                    R.array.circle_default_color_sequence);
        }
        if (isInEditMode()) {
            mColors = new int[1];
            mColors[0] = getResources().getColor(R.color.circle_progress_color);
        } else {
            if (colorArrayId != -1) {
                mColors = getResources().getIntArray(colorArrayId);
            }
        }
        a.recycle();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Cap.ROUND);
        mPaint.setStrokeWidth(mBorderWidth);
        setColors(mColors);
        setupAnimations();
    }

    public void start() {
        if (isRunning()) {
            return;
        }
        mRunning = true;
        if (mObjectAnimatorAngle.isPaused()) {
            mObjectAnimatorAngle.resume();
        } else {
            mObjectAnimatorAngle.start();
        }
        if (mObjectAnimatorSweep.isPaused()) {
            mObjectAnimatorSweep.resume();
        } else {
            mObjectAnimatorSweep.start();
        }
        invalidate();
    }

    public void stop() {
        if (!isRunning()) {
            return;
        }
        mRunning = false;
        mObjectAnimatorAngle.pause();
        mObjectAnimatorSweep.pause();
        invalidate();
    }

    public boolean isRunning() {
        return mRunning;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int sizeW = MeasureSpec.getSize(widthMeasureSpec);
        int modeW = MeasureSpec.getMode(widthMeasureSpec);
        int sizeH = MeasureSpec.getSize(heightMeasureSpec);
        int modeH = MeasureSpec.getMode(heightMeasureSpec);
        if (modeW == MeasureSpec.AT_MOST) {
            sizeW = dip2px(30);
        }
        if (modeH == MeasureSpec.AT_MOST) {
            sizeH = dip2px(30);
        }
        setMeasuredDimension(MeasureSpec.makeMeasureSpec(sizeW, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(sizeH, MeasureSpec.EXACTLY));
    }

    private int dip2px(float dipValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == VISIBLE) {
            start();
        } else {
            stop();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        start();
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        stop();
        super.onDetachedFromWindow();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        fBounds.left = mBorderWidth / 2f + .5f + getPaddingLeft();
        fBounds.right = w - mBorderWidth / 2f - .5f - getPaddingRight();
        fBounds.top = mBorderWidth / 2f + .5f + getPaddingTop();
        fBounds.bottom = h - mBorderWidth / 2f - .5f - getPaddingBottom();
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        float startAngle = mCurrentGlobalAngle - mCurrentGlobalAngleOffset;
        float sweepAngle = mCurrentSweepAngle;
        if (mModeAppearing) {
            //出现模式
            mPaint.setColor(gradient(mColors[mCurrentColorIndex], mColors[mNextColorIndex],
                    mCurrentSweepAngle / (360 - minSweepAngle * 2)));
            sweepAngle += minSweepAngle;
        } else {
            sweepAngle = mCurrentSweepAngle;
            startAngle = startAngle + sweepAngle;
            sweepAngle = 360 - sweepAngle - minSweepAngle;
        }
        canvas.drawArc(fBounds, startAngle, sweepAngle, false, mPaint);
    }

    private static int gradient(int color1, int color2, float p) {
        int r1 = (color1 & 0xff0000) >> 16;
        int g1 = (color1 & 0xff00) >> 8;
        int b1 = color1 & 0xff;
        int r2 = (color2 & 0xff0000) >> 16;
        int g2 = (color2 & 0xff00) >> 8;
        int b2 = color2 & 0xff;
        int newr = (int) (r2 * p + r1 * (1 - p));
        int newg = (int) (g2 * p + g1 * (1 - p));
        int newb = (int) (b2 * p + b1 * (1 - p));
        return Color.argb(255, newr, newg, newb);
    }

    private void toggleAppearingMode() {
        mModeAppearing = !mModeAppearing;
        if (mModeAppearing) {
            mCurrentColorIndex = ++mCurrentColorIndex % mColors.length;
            mNextColorIndex = ++mNextColorIndex % mColors.length;
            mCurrentGlobalAngleOffset = (mCurrentGlobalAngleOffset + minSweepAngle * 2) % 360;
        }
    }

    private Property<CircleProgressView, Float> mAngleProperty = new Property<CircleProgressView, Float>(Float.class, "angle") {
        @Override
        public Float get(CircleProgressView object) {
            return object.getCurrentGlobalAngle();
        }

        @Override
        public void set(CircleProgressView object, Float value) {
            object.setCurrentGlobalAngle(value);
        }
    };

    private Property<CircleProgressView, Float> mSweepProperty = new Property<CircleProgressView, Float>(Float.class, "arc") {
        @Override
        public Float get(CircleProgressView object) {
            return object.getCurrentSweepAngle();
        }

        @Override
        public void set(CircleProgressView object, Float value) {
            if (value < 0) {
                if (!object.mModeAppearing) {
                    object.toggleAppearingMode();
                }
                object.setCurrentSweepAngle(360f - minSweepAngle * 2 + value);
            } else if (value > 0) {
                if (object.mModeAppearing) {
                    object.toggleAppearingMode();
                }
                object.setCurrentSweepAngle(value);
            }

        }
    };

    private void setupAnimations() {
        mObjectAnimatorAngle = ObjectAnimator.ofFloat(this, mAngleProperty, 360f);
        mObjectAnimatorAngle.setInterpolator(ANGLE_INTERPOLATOR);
        mObjectAnimatorAngle.setDuration(angleAnimatorDuration);
        mObjectAnimatorAngle.setRepeatMode(ValueAnimator.RESTART);
        mObjectAnimatorAngle.setRepeatCount(ValueAnimator.INFINITE);

        mObjectAnimatorSweep = ObjectAnimator.ofFloat(this, mSweepProperty, -(360f - minSweepAngle * 2), 360f - minSweepAngle * 2);
        mObjectAnimatorSweep.setInterpolator(SWEEP_INTERPOLATOR);
        mObjectAnimatorSweep.setDuration(sweepAnimatorDuration * 2);
        mObjectAnimatorSweep.setRepeatMode(ValueAnimator.RESTART);
        mObjectAnimatorSweep.setRepeatCount(ValueAnimator.INFINITE);

    }

    /**
     * 设置进度条颜色
     */
    public void setColor(int color) {
        setColors(new int[]{color});
    }

    /**
     * 设置进度条颜色
     */
    public void setColors(int[] colors) {
        this.mColors = colors;
        mCurrentColorIndex = 0;
        mNextColorIndex = mColors.length > 1 ? 1 : 0;
        mPaint.setColor(mColors[mCurrentColorIndex]);
    }

    public void setCurrentGlobalAngle(float currentGlobalAngle) {
        mCurrentGlobalAngle = currentGlobalAngle;
        invalidate();
    }

    public float getCurrentGlobalAngle() {
        return mCurrentGlobalAngle;
    }

    public void setCurrentSweepAngle(float currentSweepAngle) {
        mCurrentSweepAngle = currentSweepAngle;
        invalidate();
    }

    public float getCurrentSweepAngle() {
        return mCurrentSweepAngle;
    }

}
