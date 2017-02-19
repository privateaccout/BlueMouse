package com.bluemouse.kid.bluemouse;

/**
 * Created by kid on 2017/2/5.
 */

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.os.Handler;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;

import static com.bluemouse.kid.bluemouse.utils.ThemeUtils.getThemePrimaryColor;

/**
 * Progress indicator behind the Floating Action Button.
 */
public class ProgressView extends View {

    private String Log_Str = "[ProgressView]";
    public int mTotalProgress;
    public int mCurrentProgress;
    public int mStartingProgress;
    private int mTargetProgress;
    public int mStepSize;

    public int mColor;
    public int mAnimationDuration;

    private boolean isAnimating;

    private final Paint paint;
    private final RectF arcBounds = new RectF();
    private OnCompletedListener mListener;

    public ProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Every attribute is initialized with a default value
        mColor = getThemePrimaryColor(context);

        mStartingProgress = 0;
        mCurrentProgress = 0;
        mTargetProgress = 0;
        mTotalProgress = 100;
        mStepSize = 10;
        mAnimationDuration = 6400;

        if (isInEditMode()) {
            mCurrentProgress = 40;
            mTargetProgress = 40;
        }

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(mColor);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        arcBounds.top = 0;
        arcBounds.bottom = MeasureSpec.getSize(heightMeasureSpec);
        arcBounds.left = 0;
        arcBounds.right = MeasureSpec.getSize(widthMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Note: The starting point will start counting from -90 degrees
        canvas.drawArc(arcBounds,
                -90 + mStartingProgress * 360 / mTotalProgress,
                mCurrentProgress * 360 / mTotalProgress,
                true, paint);
    }

    /*
    * Getters and Setters
    * */

    public void setCurrentProgress(int currentProgress, boolean animate) {
        // If the view is animating no further actions are allowed
        if (isAnimating){
            return;
        }

        if (this.mTargetProgress > mTotalProgress)
            this.mTargetProgress = mTotalProgress;
        else
            this.mTargetProgress = currentProgress;

        if (animate && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Animations are only available from API 11 and forth
            ValueAnimator va = ValueAnimator.ofInt(mCurrentProgress, mTargetProgress);
            va.setInterpolator(new AccelerateDecelerateInterpolator());
            va.setDuration(mAnimationDuration);
            va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @TargetApi(Build.VERSION_CODES.HONEYCOMB)
                public void onAnimationUpdate(ValueAnimator animation) {
                    mCurrentProgress = (Integer) animation.getAnimatedValue();
                    ProgressView.this.invalidate();
                }
            });
            va.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    isAnimating = false;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    isAnimating = false;
                    //if (mTargetProgress == mTotalProgress){
                        mListener.onProgressCompleted();
                    //}
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    isAnimating = false;
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                    isAnimating = false;
                }
            });
            va.start();
        } else {
            mCurrentProgress = mTargetProgress;
            invalidate();
        }

    }

    public void next(boolean animate) {
        setCurrentProgress(mCurrentProgress + mStepSize, animate);
    }

    public void setListener(OnCompletedListener mListener) {
        this.mListener = mListener;
    }

    public void setColor(int mColor) {
        this.mColor = mColor;
        paint.setColor(mColor);
        invalidate();
    }

    /*
    * Interface callbacks
    * */

    public interface OnCompletedListener {
        void onProgressCompleted();
    }
}
