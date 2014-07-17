
package io.github.wangmuy.pathanimtest;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.konka.pathanimtest.R;

import io.github.wangmuy.animUtils.PathEvaluator;
import io.github.wangmuy.animUtils.PathPoint;
import io.github.wangmuy.animUtils.PathSentinel;

public class MainActivity extends Activity implements View.OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private ViewGroup mDrawArea;
    private EditText mEditStartX;
    private EditText mEditStartY;
    private EditText mEditEndX;
    private EditText mEditEndY;
    private EditText mEditC0x;
    private EditText mEditC0y;
    private EditText mEditC1x;
    private EditText mEditC1y;
    private Button mApplyBtn;
    private ObjectAnimator mMoveSprite;
    private ParticleView mParticleView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDrawArea = (ViewGroup) findViewById(R.id.drawArea);
        mEditStartX = (EditText) findViewById(R.id.startX);
        mEditStartY = (EditText) findViewById(R.id.startY);
        mEditEndX = (EditText) findViewById(R.id.endX);
        mEditEndY = (EditText) findViewById(R.id.endY);
        mEditC0x = (EditText) findViewById(R.id.c0x);
        mEditC0y = (EditText) findViewById(R.id.c0y);
        mEditC1x = (EditText) findViewById(R.id.c1x);
        mEditC1y = (EditText) findViewById(R.id.c1y);
        mApplyBtn = (Button) findViewById(R.id.applyBtn);
        mApplyBtn.setOnClickListener(this);

/*        final ParticleView.OnCreateParticleListener factory = new ParticleView.OnCreateParticleListener() {
            final Random rand = new Random();
            @Override
            public Particle onCreateParticle() {
                final Particle p = new Particle();
                final BitmapDrawable pDrawable = (BitmapDrawable)getResources().getDrawable(R.drawable.ic_launcher);
                p.mBitmap = pDrawable.getBitmap();
                p.centerX = pDrawable.getIntrinsicWidth()/2;
                p.centerY = pDrawable.getIntrinsicHeight()/2;
                int begX = rand.nextInt(500);
                int begY = rand.nextInt(500);
                p.pathSentinel = PathSentinel.startsWith(begX, begY).curveTo(500, begY, begX, 450, begX+500, begY+500);
                p.x = begX;
                p.y = begY;
                p.pathTimeRecorder = new AnimateTimeRecorder(3000);
                p.pathTimeRecorder.setRepeatMode(ValueAnimator.REVERSE);

                //p.isFollowHeading = true;
                p.rotationSentinel = RotationSentinel.startsWith(0f).rotateTo(360f);
                p.rotation = 0;
                p.rotateTimeRecorder = new AnimateTimeRecorder(5000 + rand.nextInt(5)*1000);
                p.rotateTimeRecorder.setRepeatMode(ValueAnimator.REVERSE);
                return p;
            }
        };
        final ValueAnimator numVariateAnimator = ValueAnimator.ofInt(10, 200, 5);
        numVariateAnimator.setInterpolator(new LinearInterpolator());
        numVariateAnimator.setRepeatCount(ValueAnimator.INFINITE);
        numVariateAnimator.setRepeatMode(ValueAnimator.REVERSE);
        numVariateAnimator.setDuration(20000);
        mParticleView = new ParticleView(this, factory, numVariateAnimator);
        mDrawArea.addView(mParticleView, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT));
        mParticleView.getAnimatorSet().start();*/
    }

    @Override
    public void onClick(View v) {
        try {
            String input;
            input = mEditStartX.getText().toString();
            final float startX = Float.parseFloat(TextUtils.isEmpty(input)?"0":input);
            input = mEditStartY.getText().toString();
            final float startY = Float.parseFloat(TextUtils.isEmpty(input)?"0":input);
            input = mEditEndX.getText().toString();
            final float endX = Float.parseFloat(TextUtils.isEmpty(input)?"0":input);
            input = mEditEndY.getText().toString();
            final float endY = Float.parseFloat(TextUtils.isEmpty(input)?"0":input);
            input = mEditC0x.getText().toString();
            final float c0x = Float.parseFloat(TextUtils.isEmpty(input)?"0":input);
            input = mEditC0y.getText().toString();
            final float c0y = Float.parseFloat(TextUtils.isEmpty(input)?"0":input);
            input = mEditC1x.getText().toString();
            final float c1x = Float.parseFloat(TextUtils.isEmpty(input)?"0":input);
            input = mEditC1y.getText().toString();
            final float c1y = Float.parseFloat(TextUtils.isEmpty(input)?"0":input);

            final float biggerSentinelX = startX>endX? startX : endX;
            final float biggerSentinelY = startY>endY? startY : endY;
            final float biggerControlX = c0x>c1x? c0x : c1x;
            final float biggerControlY = c0y>c1y? c0y : c1y;
            final float maxX = biggerSentinelX>biggerControlX? biggerSentinelX : biggerControlX;
            final float maxY = biggerSentinelY>biggerControlY? biggerSentinelY : biggerControlY;

            final PathSentinel path = PathSentinel.startsWith(startX, startY)
                    .curveTo(c0x, c0y, c1x, c1y, endX, endY).lineTo(startX, startY);
            final Object[] pathPoints = path.getValues().toArray();
            final PathEvaluator eval = new PathEvaluator();
            final Paint paint = new Paint();
            paint.setStyle(Paint.Style.STROKE);

            Bitmap bgBitmap = Bitmap.createBitmap((int)(maxX+1), (int)(maxY+1), Bitmap.Config.ARGB_8888);
            Canvas bgCanvas = new Canvas(bgBitmap);
            paint.setStrokeWidth(10.0f);
            paint.setColor(0xff00ff00);
            bgCanvas.drawPoint(c0x, c0y, paint);
            paint.setColor(0xff0000ff);
            bgCanvas.drawPoint(c1x, c1y, paint);

            paint.setStrokeWidth(1.0f);
            paint.setColor(0xffff0000);
            PathPoint lastPoint = (PathPoint)pathPoints[0];
            for(float i=0.0f; i < 1.0f; i += 0.001) {
                final PathPoint p = eval.evaluate(i, (PathPoint)pathPoints[0], (PathPoint)pathPoints[1]);
                bgCanvas.drawLine(lastPoint.getX(), lastPoint.getY(), p.getX(), p.getY(), paint);
                lastPoint = p;
            }
            if(mMoveSprite != null)
                mMoveSprite.cancel();
            mDrawArea.removeAllViews();
            ImageView bg = new ImageView(this);
            bg.setBackgroundColor(0xff555555);
            bg.setImageBitmap(bgBitmap);
            mDrawArea.addView(bg, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT));

            final ImageView sprite = new ImageView(this);
            sprite.setImageResource(R.drawable.ic_launcher);
            mDrawArea.addView(sprite, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT));
            final BitmapDrawable spriteDrawable = (BitmapDrawable)sprite.getDrawable();
            final int spriteWidth = spriteDrawable.getIntrinsicWidth();
            final int spriteHeight = spriteDrawable.getIntrinsicHeight();
            Log.d(TAG, "spriteWidth=" + spriteWidth + ", spriteHeight=" + spriteHeight);
            sprite.setTranslationX(startX-spriteWidth/2);
            sprite.setTranslationY(startY-spriteHeight/2);
            mMoveSprite = ObjectAnimator.ofObject(new Object() {
                public void setTranslation(PathPoint newLoc) {
                    sprite.setTranslationX(newLoc.getX()-spriteWidth/2);
                    sprite.setTranslationY(newLoc.getY()-spriteHeight/2);
                    sprite.setRotation(newLoc.getBearing());
                }
            }, "translation", eval, pathPoints);
            /*Keyframe[] keyframes = new Keyframe[path.getKeyframes().size()];
            final PropertyValuesHolder pvh = PropertyValuesHolder.ofKeyframe("translation", path.getKeyframes().toArray(keyframes));
            pvh.setEvaluator(eval);
            mMoveSprite = ObjectAnimator.ofPropertyValuesHolder(new Object() {
                public void setTranslation(PathPoint newLoc) {
                    sprite.setTranslationX(newLoc.getX()-spriteWidth/2);
                    sprite.setTranslationY(newLoc.getY()-spriteHeight/2);
                    sprite.setRotation(newLoc.getBearing());
                }
            }, pvh);*/
            mMoveSprite.setInterpolator(new LinearInterpolator());
            mMoveSprite.setDuration(3000);
            mMoveSprite.setRepeatMode(ValueAnimator.REVERSE);
            mMoveSprite.setRepeatCount(ValueAnimator.INFINITE);
            mMoveSprite.start();
        } catch(NumberFormatException e) {
            Toast.makeText(this, "Please input float point number!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        if(mMoveSprite != null)
            mMoveSprite.cancel();
        if(mParticleView != null)
            mParticleView.getAnimatorSet().cancel();
        Log.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

}
