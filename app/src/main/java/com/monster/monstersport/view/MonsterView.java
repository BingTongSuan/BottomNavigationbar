package com.monster.monstersport.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.monster.monstersport.R;

/**
 * Created by ZhaoZongyao on 2017/10/23.
 */

public class MonsterView extends View {

    private String TAG = "MonsterView";

    private int mStartAngle = 320;
    private int mLetterColor;
    private int mLetterBackgroundColor;
    private int mRadius = 50;
    private int mStrokeWidth = 25;

    private int mReduceDuration = 100;
    private int mAnimDuration = 500;

    //正常状态  显示字母G
    private static final int STATE_NORMAL = 1;
    //减少阶段
    private static final int STATE_REDUCE = 2;
    //加载状态 变为缺口圆圈
    private static final int STATE_LOADING = 4;
    //加载完成状态 缺口圆圈变小
    private static final int STATE_COMPLETE = 5;

    private int mCurrentState = STATE_NORMAL;

    private Paint mLetterPaint;
    private Paint mBackgroundPaint;
    //当前进度 范围 0f~1f
    private float mPercent = 0f;

    private int mWidth;
    private int mHeight;
    private RectF mRectF;
    private Path mLetterPath;
    private Path mDstPath;
    private Path mDstNextPath;
    private PathMeasure mPathMeasure;

    private ValueAnimator mReduceAnimator;
    private ValueAnimator mLoadingAnimator;

    public MonsterView(Context context) {
        this(context, null);
    }

    public MonsterView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MonsterView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.MonsterView);
        mLetterColor = array.getColor(R.styleable.MonsterView_mv_letter_color, Color.parseColor("#4682B4"));
        mLetterBackgroundColor = array.getColor(R.styleable.MonsterView_mv_letter_background_color, Color.parseColor("#F4F4F4"));
        mStrokeWidth = array.getDimensionPixelSize(R.styleable.MonsterView_mv_stroke_width, 25);
        mRadius = array.getDimensionPixelSize(R.styleable.MonsterView_mv_corner_radius, 50);
        mStartAngle = array.getInt(R.styleable.MonsterView_mv_startangle, 290);
        array.recycle();

        init();
    }


    private void init() {
        mLetterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLetterPaint.setStyle(Paint.Style.STROKE);
        mLetterPaint.setStrokeWidth(mStrokeWidth);
        mLetterPaint.setStrokeCap(Paint.Cap.ROUND);
        mLetterPaint.setColor(mLetterColor);
        mRectF = new RectF();
        mLetterPath = new Path();
        mDstPath = new Path();
        mDstNextPath = new Path();
        mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackgroundPaint.setColor(mLetterBackgroundColor);
        mBackgroundPaint.setStyle(Paint.Style.STROKE);
        mBackgroundPaint.setStrokeWidth(mStrokeWidth);
        mBackgroundPaint.setStrokeCap(Paint.Cap.ROUND);

        mPathMeasure = new PathMeasure();
        initReduceAnimator();
        initLoadingAnimator();
    }


    private void initReduceAnimator() {
        mReduceAnimator = ValueAnimator.ofFloat(0f, 1f);
        mReduceAnimator.setDuration(mReduceDuration);
        mReduceAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                invalidate();
            }
        });
        mReduceAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentState = STATE_LOADING;
                if (!mLoadingAnimator.isStarted()) {
                    mLoadingAnimator.start();
                }
                invalidate();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }


    private void initLoadingAnimator() {
        mLoadingAnimator = ValueAnimator.ofFloat(0, 1f);
        mLoadingAnimator.setDuration(mAnimDuration);
        mLoadingAnimator.setInterpolator(new LinearInterpolator());
        mLoadingAnimator.setRepeatCount(-1);
        mLoadingAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                invalidate();
            }
        });
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        if (widthMode == MeasureSpec.AT_MOST) {
            mWidth = 200;
        } else {
            mWidth = MeasureSpec.getSize(widthMeasureSpec);
        }
        mHeight = mWidth;

        if (mRadius > mWidth / 2) {
            mRadius = mWidth / 2 - mStrokeWidth / 2;
        }
        setMeasuredDimension(mWidth, mHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        switch (mCurrentState) {
            case STATE_NORMAL:
                drawNormal(canvas);
                break;
            case STATE_REDUCE:
                drawReduce(canvas);
                break;
            case STATE_LOADING:
                drawLoading(canvas);
                break;
            case STATE_COMPLETE:
                drawComplete(canvas);
                break;
            default:
                drawNormal(canvas);
        }
    }

    /**
     * 画拐角
     */
    private void drawCorner() {


    }


    /**
     * 正常状态
     */
    private void drawNormal(Canvas canvas) {
        mLetterPath.reset();
        mRectF.set(mWidth / 2 - mRadius, mHeight / 2 - mRadius, mWidth / 2 + mRadius, mHeight / 2 + mRadius);
        mLetterPath.addArc(mRectF, 0, mStartAngle);

        mDstPath.reset();
        mDstNextPath.reset();
        mPathMeasure.setPath(mLetterPath, false);
        float length = mPathMeasure.getLength();
        float currentLength = length * mPercent;
        mPathMeasure.getSegment(0, currentLength, mDstPath, true);
        mPathMeasure.getSegment(currentLength, length, mDstNextPath, true);
        canvas.drawPath(mDstNextPath, mBackgroundPaint);
        canvas.drawPath(mDstPath, mLetterPaint);
        canvas.drawLine(mWidth / 2 + mRadius, mHeight / 2, mWidth / 2, mHeight / 2, mLetterPaint);

    }

    /**
     * STATE_REDUCE
     */
    private void drawReduce(Canvas canvas) {

        mLetterPath.reset();
        mRectF.set(mWidth / 2 - mRadius, mHeight / 2 - mRadius, mWidth / 2 + mRadius, mHeight / 2 + mRadius);
        mLetterPath.addArc(mRectF, 0, mStartAngle);

        float value = (float) mReduceAnimator.getAnimatedValue();
        canvas.drawLine(mWidth / 2 + mRadius * value, mHeight / 2, mWidth / 2 + mRadius, mHeight / 2, mLetterPaint);

        canvas.drawPath(mLetterPath, mLetterPaint);
    }

    /**
     * 加载状态
     */
    private void drawLoading(Canvas canvas) {
        mLetterPath.reset();
        mRectF.set(mWidth / 2 - mRadius, mHeight / 2 - mRadius, mWidth / 2 + mRadius, mHeight / 2 + mRadius);
        float value = (float) mLoadingAnimator.getAnimatedValue();
        mLetterPath.addArc(mRectF, 360 * value, mStartAngle);
        canvas.drawPath(mLetterPath, mLetterPaint);
    }

    /**
     * 完成状态
     */
    private void drawComplete(Canvas canvas) {
        mLetterPaint.setStrokeWidth(mStrokeWidth / 3);
        mLetterPath.reset();
        mRectF.set(mWidth / 2 - mRadius, mHeight / 2 - mRadius, mWidth / 2 + mRadius, mHeight / 2 + mRadius);
        float value = (float) mLoadingAnimator.getAnimatedValue();
        mLetterPath.addArc(mRectF, 360 * value, 360);
        canvas.drawPath(mLetterPath, mLetterPaint);
    }

    /**
     * 设置当前进度
     * percent 值越大，那么dstPath长度越短
     */
    public void setPercent(float percent) {
        if (percent < 0)
            return;
        if (mCurrentState != STATE_NORMAL)
            return;
        this.mPercent = percent;
        if (mPercent == 1) {
            mCurrentState = STATE_REDUCE;
            mReduceAnimator.start();
        }
        invalidate();
    }

    /**
     * 设置加载完成
     */
    public void complete() {
        if (mCurrentState == STATE_LOADING) {
            this.mCurrentState = STATE_COMPLETE;
            invalidate();
        } else {
            reset();
        }
    }


    /**
     * 重置状态、进度
     */
    public void reset() {
        this.mPercent = 0;
        mCurrentState = STATE_NORMAL;
        mReduceAnimator.cancel();
        mLoadingAnimator.cancel();
        invalidate();
    }
}