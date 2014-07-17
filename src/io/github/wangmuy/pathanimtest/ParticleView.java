package io.github.wangmuy.pathanimtest;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.TimeAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.view.View;

import io.github.wangmuy.animUtils.AnimateTimeRecorder;
import io.github.wangmuy.animUtils.PathPoint;
import io.github.wangmuy.animUtils.PathSentinel;
import io.github.wangmuy.animUtils.RotationSentinel;

import java.util.ArrayList;

public class ParticleView extends View implements TimeAnimator.TimeListener, ValueAnimator.AnimatorUpdateListener {
    public static class Particle {
        public Bitmap mBitmap;
        public int centerX;
        public int centerY;
        public float x;
        public float y;

        public float rotation = 0f;
        public RotationSentinel rotationSentinel;
        public AnimateTimeRecorder rotateTimeRecorder;
        public boolean isFollowBearing = false;

        public PathSentinel pathSentinel;
        public AnimateTimeRecorder pathTimeRecorder;
    }

    public interface OnCreateParticleListener {
        public Particle onCreateParticle();
    }

    private static final String TAG = ParticleView.class.getSimpleName();

    private ArrayList<Particle> mParticles;

    private final TimeAnimator mAnimator = new TimeAnimator();
    private ValueAnimator mNumVariateAnimator;
    private final AnimatorSet mAnimatorSet = new AnimatorSet();
    private OnCreateParticleListener mOnCreateParticleListener;

    private final Matrix mMatrix = new Matrix();
    private final Paint mPaint = new Paint();
    PathPoint mTmpPoint = PathPoint.moveTo(0, 0);

    public ParticleView(Context context, OnCreateParticleListener onCreateListener, int... numVariates) {
        super(context);
        final ValueAnimator numVariateAnimator = ValueAnimator.ofInt(numVariates);
        long duration = 3000;
        if(numVariates == null || numVariates.length <= 1)
            duration = 0;
        numVariateAnimator.setDuration(duration);
        init(onCreateListener, numVariateAnimator);
    }

    public ParticleView(Context context, OnCreateParticleListener onCreateListener, ValueAnimator numVariateAnimator) {
        super(context);
        init(onCreateListener, numVariateAnimator);
    }

    private void init(OnCreateParticleListener onCreateListener, ValueAnimator numVariateAnimator) {
        mParticles = new ArrayList<Particle>();

        mAnimator.setTimeListener(this);
        final AnimatorSet.Builder builder = mAnimatorSet.play(mAnimator);
        mNumVariateAnimator = numVariateAnimator;
        mNumVariateAnimator.addUpdateListener(this);
        builder.with(mNumVariateAnimator);
        mOnCreateParticleListener = onCreateListener;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(mParticles == null)
            return;

        mPaint.setAlpha((int) (getAlpha()*255));
        for(int i=0; i < mParticles.size(); ++i) {
            final Particle p = mParticles.get(i);
            mMatrix.setTranslate(-p.centerX, -p.centerY);
            mMatrix.postRotate(p.rotation);
            mMatrix.postTranslate(p.centerX + p.x, p.centerY + p.y);
            canvas.drawBitmap(p.mBitmap, mMatrix, mPaint);
        }
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        final int targetNum = (Integer)animation.getAnimatedValue();
        if(targetNum < 0)
            return;
        final int size = mParticles.size();
        if(targetNum == size)
            return;
        if(targetNum > size) {
            for(int i=size; i < targetNum; ++i) {
                final Particle p = mOnCreateParticleListener.onCreateParticle();
                mParticles.add(p);
            }
        } else {
            for(int i=size-1; i >= targetNum; --i) {
                mParticles.remove(i);
            }
        }

    }

    @Override
    public void onTimeUpdate(TimeAnimator animation, long totalTime, long deltaTime) {
        if(mParticles.size() == 0)
            return;

        final int size = mParticles.size();
        for(int i=0; i < size; ++i) {
            final Particle p = mParticles.get(i);
            p.pathTimeRecorder.update(deltaTime);
            final PathPoint curLoc = p.pathSentinel.getValueTo(mTmpPoint, p.pathTimeRecorder.getFraction());
            p.x = curLoc.getX();
            p.y = curLoc.getY();

            if(p.isFollowBearing) {
                p.rotation = curLoc.getBearing();
            } else {
                p.rotateTimeRecorder.update(deltaTime);
                p.rotation = (Float) p.rotationSentinel.getValue(p.rotateTimeRecorder.getFraction());
            }
        }

        invalidate();
    }

    public Animator getAnimatorSet() {
        return mAnimatorSet;
    }

}
