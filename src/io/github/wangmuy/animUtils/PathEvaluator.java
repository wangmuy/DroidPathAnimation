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

import android.animation.TypeEvaluator;

/**
 * This evaluator interpolates between two PathPoint values given the value t, the
 * proportion traveled between those points. The value of the interpolation depends
 * on the operation specified by the endValue (the operation for the interval between
 * PathPoints is always specified by the end point of that interval).
 */
public class PathEvaluator implements TypeEvaluator<PathPoint> {
    private static final String TAG = PathEvaluator.class.getSimpleName();
    @Override
    public PathPoint evaluate(float t, PathPoint startValue, PathPoint endValue) {
        //Log.d(TAG, "eval frac=" + t + " between " + startValue + " and " + endValue);
        final PathPoint ret = new PathPoint();
        evaluateTo(ret, t, startValue, endValue);
        return ret;
    }

    public PathPoint evaluateTo(PathPoint point, float t, PathPoint startValue, PathPoint endValue) {
        float x, y, dx=0, dy=0, bearing=0;
        if (endValue.mOperation == PathPoint.CURVE) {
            // P(t) = (1 - t)^3 * P0 + 3t(1-t)^2 * P1 + 3t^2 (1-t) * P2 + t^3 * P3
            float oneMinusT = 1 - t;
            x = oneMinusT * oneMinusT * oneMinusT * startValue.mX +
                    3 * oneMinusT * oneMinusT * t * endValue.mControl0X +
                    3 * oneMinusT * t * t * endValue.mControl1X +
                    t * t * t * endValue.mX;
            y = oneMinusT * oneMinusT * oneMinusT * startValue.mY +
                    3 * oneMinusT * oneMinusT * t * endValue.mControl0Y +
                    3 * oneMinusT * t * t * endValue.mControl1Y +
                    t * t * t * endValue.mY;

            //dP(t) / dt = -3(1-t)^2 * P0 + 3(1-t)^2 * P1 - 6t(1-t) * P1 - 3t^2 * P2 + 6t(1-t) * P2 + 3t^2 * P3
            dx = -3 * oneMinusT * oneMinusT * startValue.mX +
                    3 * oneMinusT * oneMinusT * endValue.mControl0X -
                    6 * t * oneMinusT * endValue.mControl0X -
                    3 * t * t * endValue.mControl1X +
                    6 * t * oneMinusT * endValue.mControl1X +
                    3 * t * t * endValue.mX;
            dy = -3 * oneMinusT * oneMinusT * startValue.mY +
                    3 * oneMinusT * oneMinusT * endValue.mControl0Y -
                    6 * t * oneMinusT * endValue.mControl0Y -
                    3 * t * t * endValue.mControl1Y +
                    6 * t * oneMinusT * endValue.mControl1Y +
                    3 * t * t * endValue.mY;
        } else if (endValue.mOperation == PathPoint.LINE) {
            dx = endValue.mX - startValue.mX;
            dy = endValue.mY - startValue.mY;
            x = startValue.mX + t * dx;
            y = startValue.mY + t * dy;
        } else {
            x = endValue.mX;
            y = endValue.mY;
        }
        if(Float.compare(dx, 0.0f) != 0) {
            double tangent = dy / dx;
            final double arctan = Math.atan(tangent);
            bearing = (float) (180 * arctan / Math.PI + (dx>0f?90:-90));
        } else if(dy > 0) {
            bearing = 180f;
        } else {
            bearing = 0f;
        }

        point.mOperation = PathPoint.MOVE;
        point.mX = x;
        point.mY = y;
        point.mBearing = bearing;

        return point;
    }
}
