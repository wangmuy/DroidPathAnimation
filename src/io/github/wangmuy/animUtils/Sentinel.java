package io.github.wangmuy.animUtils;

import android.animation.Keyframe;
import android.animation.TimeInterpolator;
import android.animation.TypeEvaluator;

import java.util.ArrayList;
import java.util.Collection;

@SuppressWarnings("unchecked")
public abstract class Sentinel<T> {
    protected ArrayList<T> mValues = new ArrayList<T>();
    protected ArrayList<Keyframe> mKeyframes = new ArrayList<Keyframe>(); // only used when there are more than 2 keyframes
    protected TimeInterpolator mInterpolator; // only used in the 2-keyframe case
    protected Keyframe mFirstKeyframe;
    protected Keyframe mLastKeyframe;

    protected TypeEvaluator<T> mEvaluator;

    protected void addKeyframe(Object value, TimeInterpolator interpolator) {
        final int size = mKeyframes.size();
        if(size == 1) {
            mInterpolator = interpolator;
        }
        final float evenedFraction = (size==0?0f:(1f/size));
        Keyframe frame = Keyframe.ofObject(evenedFraction*size, value);
        frame.setInterpolator(interpolator);
        mKeyframes.add(frame);
        float evenedSum = 0f;
        for(int i=1; i < size; ++i) {
            evenedSum += evenedFraction;
            mKeyframes.get(i).setFraction(evenedSum);
        }
        if(size == 0)
            mFirstKeyframe = frame;
        mLastKeyframe = frame;
    }

    public void setEvaluator(TypeEvaluator<T> evaluator) {
        mEvaluator = evaluator;
    }

    public TypeEvaluator<T> getEvaluator() {
        return mEvaluator;
    }

    /**
     * Gets the animated value, given the elapsed fraction of the animation (interpolated by the
     * animation's interpolator) and the evaluator used to calculate in-between values. This
     * function maps the input fraction to the appropriate sentinel interval and a fraction
     * between them and returns the interpolated value. Note that the input fraction may fall
     * outside the [0-1] bounds, if the animation's sentinel made that happen (e.g., a
     * spring interpolation that might send the fraction past 1.0). We handle this situation by
     * just using the two sentinels at the appropriate end when the value is outside those bounds.
     *
     * @param fraction The elapsed fraction of the animation
     * @return The animated value.
     */
    public Object getValue(float fraction) {
        final int numKeyframes = mKeyframes.size();
        // Special-case optimization for the common case of only two keyframes
        if (numKeyframes == 2) {
            if (mInterpolator != null) {
                fraction = mInterpolator.getInterpolation(fraction);
            }
            return mEvaluator.evaluate(fraction, (T)mFirstKeyframe.getValue(),
                    (T)mLastKeyframe.getValue());
        }

        if (fraction <= 0f) {
            final Keyframe nextKeyframe = mKeyframes.get(1);
            final TimeInterpolator interpolator = nextKeyframe.getInterpolator();
            if (interpolator != null) {
                fraction = interpolator.getInterpolation(fraction);
            }
            final float prevFraction = mFirstKeyframe.getFraction();
            float intervalFraction = (fraction - prevFraction) /
                    (nextKeyframe.getFraction() - prevFraction);
            return mEvaluator.evaluate(intervalFraction, (T)mFirstKeyframe.getValue(),
                    (T)nextKeyframe.getValue());
        } else if (fraction >= 1f) {
            final Keyframe prevKeyframe = mKeyframes.get(numKeyframes - 2);
            final TimeInterpolator interpolator = mLastKeyframe.getInterpolator();
            if (interpolator != null) {
                fraction = interpolator.getInterpolation(fraction);
            }
            final float prevFraction = prevKeyframe.getFraction();
            float intervalFraction = (fraction - prevFraction) /
                    (mLastKeyframe.getFraction() - prevFraction);
            return mEvaluator.evaluate(intervalFraction, (T)prevKeyframe.getValue(),
                    (T)mLastKeyframe.getValue());
        }
        Keyframe prevKeyframe = mFirstKeyframe;
        for (int i = 1; i < numKeyframes; ++i) {
            Keyframe nextKeyframe = mKeyframes.get(i);
            if (fraction < nextKeyframe.getFraction()) {
                final TimeInterpolator interpolator = nextKeyframe.getInterpolator();
                if (interpolator != null) {
                    fraction = interpolator.getInterpolation(fraction);
                }
                final float prevFraction = prevKeyframe.getFraction();
                float intervalFraction = (fraction - prevFraction) /
                        (nextKeyframe.getFraction() - prevFraction);
                return mEvaluator.evaluate(intervalFraction, (T)prevKeyframe.getValue(),
                        (T)nextKeyframe.getValue());
            }
            prevKeyframe = nextKeyframe;
        }
        // shouldn't reach here
        return mLastKeyframe.getValue();
    }

    public Collection<T> getValues() {
        return mValues;
    }

    public Collection<Keyframe> getKeyframes() {
        return mKeyframes;
    }
}
