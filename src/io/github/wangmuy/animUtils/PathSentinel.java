/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.wangmuy.animUtils;

import android.animation.Keyframe;
import android.animation.TimeInterpolator;

/**
 * A simple Path object that holds information about the points along
 * a path. The API allows you to specify a move location (which essentially
 * jumps from the previous point in the path to the new one), a line location
 * (which creates a line segment from the previous location) and a curve
 * location (which creates a cubic Bézier curve from the previous location).
 */
public class PathSentinel extends Sentinel<PathPoint> {
    private static final PathEvaluator sEvaluator = new PathEvaluator();

    public static PathSentinel startsWith(float x, float y) {
        return new PathSentinel(x, y);
    }

    private PathSentinel(float x, float y) {
        moveTo(x, y);
        setEvaluator(sEvaluator);
    }

    public PathSentinel moveTo(float x, float y, TimeInterpolator interpolator) {
        final PathPoint point = PathPoint.moveTo(x, y);
        mValues.add(point);
        addKeyframe(point, interpolator);
        return this;
    }
    /**
     * Move from the current path point to the new one
     * specified by x and y. This will create a discontinuity if this point is
     * neither the first point in the path nor the same as the previous point
     * in the path.
     */
    public PathSentinel moveTo(float x, float y) {
        final PathPoint point = PathPoint.moveTo(x, y);
        mValues.add(point);
        addKeyframe(point, null);
        return this;
    }

    public PathSentinel lineTo(float x, float y, TimeInterpolator interpolator) {
        final PathPoint point = PathPoint.lineTo(x, y);
        mValues.add(point);
        addKeyframe(point, interpolator);
        return this;
    }

    /**
     * Create a straight line from the current path point to the new one
     * specified by x and y.
     */
    public PathSentinel lineTo(float x, float y) {
        final PathPoint point = PathPoint.lineTo(x, y);
        mValues.add(point);
        addKeyframe(point, null);
        return this;
    }

    public PathSentinel curveTo(float c0X, float c0Y, float c1X, float c1Y, float x, float y, TimeInterpolator interpolator) {
        final PathPoint point = PathPoint.curveTo(c0X, c0Y, c1X, c1Y, x, y);
        mValues.add(point);
        addKeyframe(point, interpolator);
        return this;
    }

    /**
     * Create a cubic Bézier curve from the current path point to the new one
     * specified by x and y. The curve uses the current path location as the first anchor
     * point, the control points (c0X, c0Y) and (c1X, c1Y), and (x, y) as the end anchor point.
     */
    public PathSentinel curveTo(float c0X, float c0Y, float c1X, float c1Y, float x, float y) {
        final PathPoint point = PathPoint.curveTo(c0X, c0Y, c1X, c1Y, x, y);
        mValues.add(point);
        addKeyframe(point, null);
        return this;
    }

    public PathPoint getValueTo(PathPoint outValue, float fraction) {
        final int numKeyframes = mKeyframes.size();
        // Special-case optimization for the common case of only two keyframes
        if (numKeyframes == 2) {
            if (mInterpolator != null) {
                fraction = mInterpolator.getInterpolation(fraction);
            }
            return ((PathEvaluator)mEvaluator).evaluateTo(outValue, fraction, (PathPoint)mFirstKeyframe.getValue(),
                    (PathPoint)mLastKeyframe.getValue());
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
            return ((PathEvaluator)mEvaluator).evaluateTo(outValue, intervalFraction, (PathPoint)mFirstKeyframe.getValue(),
                    (PathPoint)nextKeyframe.getValue());
        } else if (fraction >= 1f) {
            final Keyframe prevKeyframe = mKeyframes.get(numKeyframes - 2);
            final TimeInterpolator interpolator = mLastKeyframe.getInterpolator();
            if (interpolator != null) {
                fraction = interpolator.getInterpolation(fraction);
            }
            final float prevFraction = prevKeyframe.getFraction();
            float intervalFraction = (fraction - prevFraction) /
                    (mLastKeyframe.getFraction() - prevFraction);
            return ((PathEvaluator)mEvaluator).evaluateTo(outValue, intervalFraction, (PathPoint)prevKeyframe.getValue(),
                    (PathPoint)mLastKeyframe.getValue());
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
                return ((PathEvaluator)mEvaluator).evaluateTo(outValue, intervalFraction, (PathPoint)prevKeyframe.getValue(),
                        (PathPoint)nextKeyframe.getValue());
            }
            prevKeyframe = nextKeyframe;
        }
        // shouldn't reach here
        return (PathPoint) mLastKeyframe.getValue();
    }
}
