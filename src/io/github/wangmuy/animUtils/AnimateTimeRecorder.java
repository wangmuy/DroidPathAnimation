package io.github.wangmuy.animUtils;

import android.animation.ValueAnimator;

public class AnimateTimeRecorder {
    protected int mRepeatMode;
    protected long mDuration;
    protected float mFraction; // [0, 1]
    protected boolean mIsOnReverse;

    public AnimateTimeRecorder(long duration) {
        mDuration = duration;
    }

    public void setRepeatMode(int repeatMode) {
        mRepeatMode = repeatMode;
    }

    public int getRepeatMode() {
        return mRepeatMode;
    }

    public long getDuration() {
        return mDuration;
    }
    
    public void setFraction(float fraction) {
        mFraction = fraction;
    }

    public float getFraction() {
        return mFraction;
    }

    public void update(long deltaTime) {
        if(Float.compare(mFraction, 0f) == 0) {
            mIsOnReverse = false;
        } else if(Float.compare(mFraction, 1f) == 0) {
            if(mRepeatMode == ValueAnimator.REVERSE)
                mIsOnReverse = true;
            else if(mRepeatMode == ValueAnimator.RESTART)
                mFraction = 0;
        }

        if(mFraction < 1f || (mFraction >= 1f && mRepeatMode != 0)) {
            float deltaFrac = mDuration>0 ? deltaTime / (float)mDuration : 1f;
            if(mIsOnReverse)
                deltaFrac = -deltaFrac;
            //Log.d(TAG, "frac from " + p._frac + " to " + (p._frac + deltaFrac));
            mFraction += deltaFrac;
            if(mFraction > 1.0f)
                mFraction = 1.0f;
            if(mFraction < 0f)
                mFraction = 0f;
        }
    }

}
