
package com.fujun.browser.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

public class HorizontalScrollBar extends TextView {

    public interface OnScrollListener {
        void onScrollStart(int startPosition);

        void onScrollEnd(int endPosition);
    }

    private Drawable mScrollBar;
    private int mPointerCount = 1;
    private int mCurrentPoint = 1;
    private int mToPoint = 0;
    private float WIDTH_PER_POINT = 0.0f;
    private int SCROLL_SPEED = 800;
    private boolean ANIMATION = false;
    private long mAnimationStartTime = 0;
    private final long NO_ANIMATION = -1;
    private boolean IN_ANIMATION = false;
    private OnScrollListener mScrollListener;

    public HorizontalScrollBar(Context context) {
        super(context);
        mScrollBar = new ColorDrawable(Color.WHITE);
        mScrollBar.setCallback(this);
    }

    public HorizontalScrollBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        mScrollBar = new ColorDrawable(Color.WHITE);
        mScrollBar.setCallback(this);
    }

    public HorizontalScrollBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mScrollBar = new ColorDrawable(Color.WHITE);
        mScrollBar.setCallback(this);
    }

    public void setScrollDrawable(int resId) {
        mScrollBar = getContext().getResources().getDrawable(resId);
        mScrollBar.setCallback(this);
    }

    public void setScrollDrawable(Drawable Drawable) {
        mScrollBar = Drawable;
        mScrollBar.setCallback(this);
    }

    public void setScrollPointCount(int pointerCount) {
        mPointerCount = pointerCount;
    }

    public void setCurrentScrollPoint(int currentPoint) {
        mCurrentPoint = currentPoint;
        ANIMATION = false;
        invalidate();
    }

    public void setOnScrollListenner(OnScrollListener scrollListenner) {
        mScrollListener = scrollListenner;
    }

    public void scrollToPoint(int point) {
        if (point > 0 && point != mCurrentPoint && point <= mPointerCount) {
            mToPoint = point;
            ANIMATION = true;
            mAnimationStartTime = AnimationUtils.currentAnimationTimeMillis();
            if (mScrollListener != null) {
                mScrollListener.onScrollStart(mCurrentPoint);
            }
            if (!IN_ANIMATION) {
                invalidate();
            }
        }
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (mScrollBar != null && mScrollBar.isStateful()) {
            mScrollBar.setState(getDrawableState());
        }
        ANIMATION = false;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        this.setHeight(mScrollBar.getIntrinsicHeight());
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
            int bottom) {
        bottom = top + mScrollBar.getIntrinsicHeight();
        super.onLayout(changed, left, top, right, bottom);
        if (mPointerCount <= 0) {
            mPointerCount = 1;
        }
        // ANIMATION = false;
        WIDTH_PER_POINT = (float) (right - left) / mPointerCount;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (ANIMATION == true && (mToPoint <= 0 || mToPoint > mPointerCount)) {
            return;
        }
        if (ANIMATION == false) {
            mScrollBar.setBounds((int) (WIDTH_PER_POINT * (mCurrentPoint - 1)), 0,
                    (int) (WIDTH_PER_POINT * mCurrentPoint), getHeight() - getPaddingBottom());
            mScrollBar.draw(canvas);
        } else {
            if (mToPoint != mCurrentPoint && mAnimationStartTime != NO_ANIMATION) {
                IN_ANIMATION = true;
                int speed = (mToPoint - mCurrentPoint) < 0 ? -SCROLL_SPEED : SCROLL_SPEED;
                long time = AnimationUtils.currentAnimationTimeMillis() - mAnimationStartTime;
                if (time < 10) {
                    time = 10;
                }
                float position = WIDTH_PER_POINT * (mCurrentPoint - 1) + speed * time / 1000;
                if (speed < 0 && position < WIDTH_PER_POINT * (mToPoint - 1)) {
                    position = WIDTH_PER_POINT * (mToPoint - 1);
                }
                if (speed > 0 && position > WIDTH_PER_POINT * (mToPoint - 1)) {
                    position = WIDTH_PER_POINT * (mToPoint - 1);
                }
                boolean done = position == WIDTH_PER_POINT * (mToPoint - 1);
                if (!done) {
                    invalidate();
                } else {
                    mAnimationStartTime = NO_ANIMATION;
                    position = position + 1;
                    mCurrentPoint = mToPoint;
                    IN_ANIMATION = false;
                    if (mScrollListener != null) {
                        mScrollListener.onScrollEnd(mCurrentPoint);
                    }
                }
                mScrollBar.setBounds((int) position, 0, (int) (position + WIDTH_PER_POINT),
                        getHeight() - getPaddingBottom());
                mScrollBar.draw(canvas);
            } else {
                mScrollBar.setBounds((int) (WIDTH_PER_POINT * (mCurrentPoint - 1)), 0,
                        (int) (WIDTH_PER_POINT * mCurrentPoint), getHeight() - getPaddingBottom());
                mScrollBar.draw(canvas);
            }
        }
    }
}
