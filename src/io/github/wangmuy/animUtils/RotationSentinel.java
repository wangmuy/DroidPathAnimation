package io.github.wangmuy.animUtils;

import android.animation.FloatEvaluator;
import android.animation.TimeInterpolator;

public class RotationSentinel extends Sentinel<Number> {
    private static final FloatEvaluator sEvaluator = new FloatEvaluator();

    public static RotationSentinel startsWith(float degree) {
        return new RotationSentinel(degree);
    }

    private RotationSentinel(float degree) {
        rotateTo(degree);
        setEvaluator(sEvaluator);
    }

    public RotationSentinel rotateTo(float degree) {
        return rotateTo(degree, null);
    }

    public RotationSentinel rotateTo(float degree, TimeInterpolator interpolator) {
        mValues.add(degree);
        addKeyframe(degree, interpolator);
        return this;
    }
}
