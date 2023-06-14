/*******************************************************************************
 * Copyright 2014-2017 Specure GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package at.specure.android.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.AsyncTask;
import android.os.Handler;
import androidx.core.content.ContextCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.specure.opennettest.R;

import timber.log.Timber;

public class CustomGauge extends View {

    private static final int DEFAULT_LONG_POINTER_SIZE = 1;
    private int mScaleCount;
    private int mScaleColor;
    private boolean showScale;

    private Paint mPaint;
    private float mStrokeInnerWidth;
    private float mStrokeCentralWidth;
    private float mStrokeOuterWidth;
    private int mStrokeInnerColor;
    private int mStrokeCentralColor;
    private int mStrokeOuterColor;
    private RectF mRect;
    private String mStrokeCap;
    private int mStartAngle;
    private int mSweepAngle;
    private int mStartValue;
    private int mEndValue;
    private int mValue;
    private double mPointAngle;
    private int mPoint;
    private int mPointSize;
    private int mPointStartColor;
    private int mPointEndColor;
    private int mDividerColor;
    private int mDividerSize;
    private int mDividerStepAngle;
    private int mDividersCount;
    private boolean mDividerDrawFirst;
    private boolean mDividerDrawLast;
    private int[] dividerPositionHidden; //indexed from 0
    private int[] colorList;
    private String[] stringList;
    private int[] stringShiftsList;
    private float stringsSize;
    private float stringPadding;
    private int gaugeValue;
    private float mScaleSize;
    private float mScaleWidth;
    private int dividerStep;
    private boolean showArrow;
    private AsyncTask<Void, Integer, Void> asyncTask;
    private float rectLeft;
    private float rectTop;
    private float rectRight;
    private float rectBottom;
    private Shader shader;
    private RectF mScaleRect;
    private float radius;
    private Paint arrowPaint;
    private Paint paintCircle;
    private Paint paintDot;
    private MyRunnable runnable;
    private boolean redrawAll;
    private Paint paintCircleInner;
    private RectF mCentralOvalRect;
    private RectF mMiddleOvalRect;
    private RectF mOuterOvalRect;
    private Paint mPaintText;
    private Path mStringArc;
    private Path mLastStringArc;
    private Path mStringArc1;
    private RectF stringOvalRect;
    private Paint paintOvalInner;
    private RectF mBaseOvalRect;
    private Paint mPaintDivider;
    private Paint paintProgress;
    private Bitmap arrowForeground;
    private float size;
    private Bitmap background;
    private Paint backgroundPaint;
    private Paint centralOvalPaint;
    private Paint outerOvalPaint;
    private Bitmap bmp;
    private Canvas backgroundCanvas;
    private Canvas foregroundCanvas;
    private Paint mScalePaint;
    private Bitmap foreground;
    private float startY;
    private float startX;

    public CustomGauge(Context context) {
        super(context);
        init();
    }

    public CustomGauge(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomGauge, 0, 0);

        // stroke style
        setmStrokeInnerWidth(a.getDimension(R.styleable.CustomGauge_gaugeStrokeInnerWidth, 10));
        setmStrokeCentralWidth(a.getDimension(R.styleable.CustomGauge_gaugeStrokeCentralWidth, 10));
        setmStrokeOuterWidth(a.getDimension(R.styleable.CustomGauge_gaugeStrokeOuterWidth, 10));

        gaugeValue = (a.getInt(R.styleable.CustomGauge_gaugeValue, 0));
        showScale = a.getBoolean(R.styleable.CustomGauge_gaugeShowScale, false);
        showArrow = a.getBoolean(R.styleable.CustomGauge_gaugeShowGaugeArrow, false);
        mScaleSize = a.getDimension(R.styleable.CustomGauge_gaugeScaleSize, 0);
        mScaleWidth = a.getDimension(R.styleable.CustomGauge_gaugeScaleWidth, 0);
        mScaleCount = a.getInt(R.styleable.CustomGauge_gaugeScaleCount, 0);
        mScaleColor = a.getColor(R.styleable.CustomGauge_gaugeScaleColor, ContextCompat.getColor(context, android.R.color.darker_gray));

        setmStrokeInnerColor(a.getColor(R.styleable.CustomGauge_gaugeStrokeInnerColor, ContextCompat.getColor(context, android.R.color.darker_gray)));
        setmStrokeCentralColor(a.getColor(R.styleable.CustomGauge_gaugeStrokeCentralColor, ContextCompat.getColor(context, android.R.color.darker_gray)));
        setmStrokeOuterColor(a.getColor(R.styleable.CustomGauge_gaugeStrokeOuterColor, ContextCompat.getColor(context, android.R.color.darker_gray)));

        setStrokeCap(a.getString(R.styleable.CustomGauge_gaugeStrokeCap));
        int progressStrokeColorsId = a.getResourceId(R.styleable.CustomGauge_gaugeColors, 0);
        if (progressStrokeColorsId != 0) {
            colorList = getResources().getIntArray(progressStrokeColorsId);
        }

        // strings
        int gaugeStringsId = a.getResourceId(R.styleable.CustomGauge_gaugeStrings, 0);
        if (gaugeStringsId != 0) {
            stringList = getResources().getStringArray(gaugeStringsId);
        }

        int gaugeStringsShiftsId = a.getResourceId(R.styleable.CustomGauge_gaugeStringsShifts, 0);
        if (gaugeStringsShiftsId != 0) {
            stringShiftsList = getResources().getIntArray(gaugeStringsShiftsId);
        }

        stringsSize = a.getDimension(R.styleable.CustomGauge_gaugeStringsSize, 40);
        stringPadding = a.getDimension(R.styleable.CustomGauge_gaugeStringsPadding, 10);

        // angle start and sweep (opposite direction 0, 270, 180, 90)
        setStartAngle(a.getInt(R.styleable.CustomGauge_gaugeStartAngle, 0));
        setSweepAngle(a.getInt(R.styleable.CustomGauge_gaugeSweepAngle, 360));

        // scale (from mStartValue to mEndValue)
        setStartValue(a.getInt(R.styleable.CustomGauge_gaugeStartValue, 0));
        setEndValue(a.getInt(R.styleable.CustomGauge_gaugeEndValue, 1000));

        // pointer size and color
        setPointSize(a.getInt(R.styleable.CustomGauge_gaugePointSize, 0));
        setPointStartColor(a.getColor(R.styleable.CustomGauge_gaugePointStartColor, ContextCompat.getColor(context, android.R.color.white)));
        setPointEndColor(a.getColor(R.styleable.CustomGauge_gaugePointEndColor, ContextCompat.getColor(context, android.R.color.white)));

        // divider options
        int dividerPositionHiddenId = a.getResourceId(R.styleable.CustomGauge_gaugeDividerPositionHidden, 0);
        if (dividerPositionHiddenId != 0) {
            dividerPositionHidden = getResources().getIntArray(dividerPositionHiddenId);
        }
        int dividerSize = a.getInt(R.styleable.CustomGauge_gaugeDividerSize, 0);
        setDividerColor(a.getColor(R.styleable.CustomGauge_gaugeDividerColor, ContextCompat.getColor(context, android.R.color.white)));
        dividerStep = a.getInt(R.styleable.CustomGauge_gaugeDividerStep, 0);
        setDividerDrawFirst(a.getBoolean(R.styleable.CustomGauge_gaugeDividerDrawFirst, true));
        setDividerDrawLast(a.getBoolean(R.styleable.CustomGauge_gaugeDividerDrawLast, true));

        // calculating one point sweep
        mPointAngle = ((double) Math.abs(mSweepAngle) / (mEndValue - mStartValue));

        // calculating divider step
        if (dividerSize > 0) {
            mDividerSize = mSweepAngle / (Math.abs(mEndValue - mStartValue) / dividerSize);
            mDividersCount = 100 / dividerStep;
            mDividerStepAngle = mSweepAngle / mDividersCount;
        }
        a.recycle();
        init();
    }

    public int getmScaleCount() {
        return mScaleCount;
    }

    public void setmScaleCount(int mScaleCount) {
        this.mScaleCount = mScaleCount;
    }

    public int getmScaleColor() {
        return mScaleColor;
    }

    public void setmScaleColor(int mScaleColor) {
        this.mScaleColor = mScaleColor;
    }

    public boolean isShowScale() {
        return showScale;
    }

    public void setShowScale(boolean showScale) {
        this.showScale = showScale;
    }

    public float getmScaleSize() {
        return mScaleSize;
    }

    public void setmScaleSize(float mScaleSize) {
        this.mScaleSize = mScaleSize;
    }

    public float getmScaleWidth() {
        return mScaleWidth;
    }

    public void setmScaleWidth(float mScaleWidth) {
        this.mScaleWidth = mScaleWidth;
    }

    private void init() {

        backgroundPaint = new Paint();
        backgroundPaint.setFilterBitmap(true);

        //main Paint
        mPaint = new Paint();
        mPaint.setColor(mStrokeInnerColor);
        mPaint.setStrokeWidth(mStrokeInnerWidth);
        setStyleToPaint(mPaint);

        mScalePaint = new Paint();
        setStyleToPaint(mScalePaint);

        paintOvalInner = new Paint();
        setStyleToPaint(paintOvalInner);
        paintOvalInner.setColor(mStrokeInnerColor);
        paintOvalInner.setShader(null);
        paintOvalInner.setStrokeWidth(mStrokeInnerWidth);

        paintProgress = new Paint();
        setStyleToPaint(paintProgress);
        paintProgress.setColor(mStrokeInnerColor);
        if (colorList != null) {
            shader = new LinearGradient(getWidth(), getHeight(), 0, 0, colorList, null, Shader.TileMode.CLAMP);
        } else {
            shader = new LinearGradient(getWidth(), getHeight(), 0, 0, mPointStartColor, mPointEndColor, Shader.TileMode.CLAMP);
        }
        paintProgress.setShader(shader);
        paintProgress.setStrokeWidth(mStrokeInnerWidth);

        mRect = new RectF();
        mCentralOvalRect = new RectF();
        mBaseOvalRect = new RectF();
        mOuterOvalRect = new RectF();

        if (mValue <= 0) {
            mValue = mStartValue;
            mPoint = mStartAngle;
        }


        mScaleRect = new RectF();
        arrowPaint = new Paint();

        // needle circles
        paintCircle = new Paint(arrowPaint);
        paintCircle.setColor(ContextCompat.getColor(getContext(), R.color.gauge_inner_ring_dark));
        paintCircleInner = new Paint(paintCircle);
        paintCircleInner.setStyle(Paint.Style.FILL);
        paintCircleInner.setColor(ContextCompat.getColor(getContext(), android.R.color.white));
        paintDot = new Paint(paintCircleInner);
        paintDot.setColor(mScaleColor);

        mPaintDivider = new Paint();
        mPaintDivider.setStrokeWidth(mStrokeInnerWidth);
        mPaintDivider.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaintDivider.setColor(mDividerColor);
        mPaintDivider.setShader(null);

        arrowPaint.setColor(ContextCompat.getColor(getContext(), R.color.gauge_pointer_color));
        arrowPaint.setAntiAlias(true);
        arrowPaint.setStrokeWidth(mScaleWidth + 5);
        arrowPaint.setStrokeCap(Paint.Cap.ROUND);

        mPaintText = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintText.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaintText.setColor(getResources().getColor(R.color.gauge_basic));
        mPaintText.setTextSize(stringsSize);

        mStringArc = new Path();
        mLastStringArc = new Path();

        stringOvalRect = new RectF();

        float padding = getmStrokeInnerWidth();
        size = getWidth() < getHeight() ? getWidth() : getHeight();
        float width = size - (2 * padding);
        float height = size - (2 * padding);
        //        float radius = (width > height ? width/2 : height/2);
        radius = (width < height ? width / 2 : height / 2);
        rectLeft = (getWidth() - (2 * padding)) / 2 - radius + padding + mStrokeCentralWidth + mStrokeOuterWidth;
        rectTop = (getHeight() - (2 * padding)) / 2 - radius + padding + mStrokeCentralWidth + mStrokeOuterWidth;
        rectRight = (getWidth() - (2 * padding)) / 2 - radius + padding + width - mStrokeCentralWidth - mStrokeOuterWidth;
        rectBottom = (getHeight() - (2 * padding)) / 2 - radius + padding + height - mStrokeCentralWidth - mStrokeOuterWidth;

        mScaleRect.set(rectLeft + mScaleWidth,
                rectTop + mScaleWidth,
                rectRight - mScaleWidth,
                rectBottom - mScaleWidth);
        mBaseOvalRect.set(rectLeft - mStrokeInnerWidth / 2,
                rectTop - mStrokeInnerWidth / 2,
                rectRight + mStrokeInnerWidth / 2,
                rectBottom + mStrokeInnerWidth / 2);
        mRect.set(rectLeft - mStrokeInnerWidth / 2,
                rectTop - mStrokeInnerWidth / 2,
                rectRight + mStrokeInnerWidth / 2,
                rectBottom + mStrokeInnerWidth / 2);


        startX = (rectRight + rectLeft) / 2;
        startY = (rectTop + rectBottom) / 2;

        //background
        prepareStaticOvals();
        prepareTexts();
        prepareScale();


        //foreground
        if (foreground != null) {
            foreground.recycle();
        }
        size = getWidth();

        if (((size > 0) && (redrawAll)) || ((size > 0) && (background == null))) {// || (foreground == null)) {
            Timber.e("GAUGE: STATIC OVALS INNER %s", size);
            try {
                Timber.e("BITMAP ORIG");
                foreground = Bitmap.createBitmap((int) size, (int) size, Bitmap.Config.ARGB_8888);
                throw new OutOfMemoryError();
            } catch (OutOfMemoryError error) {
//                if (foreground != null) {
//                    foreground.recycle();
//                }
//                Timber.e("BITMAP", "2x DOWNSCALE");
//                try {
//                    int size2 = (int) (size/2);
//                    foreground = Bitmap.createBitmap((int) size2, (int) size2, Bitmap.Config.ARGB_8888);
//                } catch (OutOfMemoryError outOfMemoryError) {
//                    if (foreground != null) {
//                        foreground.recycle();
//                    }
//                    int size3 = (int) (size/4);
//                    foreground = Bitmap.createBitmap((int) size3, (int) size3, Bitmap.Config.ARGB_8888);
//                    Timber.e("BITMAP", "10x DOWNSCALE");
//                }
            }
            if(foreground!=null)
                foregroundCanvas = new Canvas(foreground);
        }

        prepareDividers();
        prepareArrow();

        redrawAll = true;
    }

    private void prepareArrow() {
        if ((foregroundCanvas != null) && (showArrow)) {
            foregroundCanvas.drawCircle(startX, startY, 25f, paintCircle);
            foregroundCanvas.drawCircle(startX, startY, 19f, paintCircleInner);
            foregroundCanvas.drawCircle(startX, startY, 8f, paintDot);
        }
    }

    private void prepareDividers() {

        if (foregroundCanvas != null) {

            if (mDividerSize > 0) {
                int actualPositionHiddenShift = 0;
                int i = actualPositionHiddenShift = mDividerDrawFirst ? 0 : 1;
                int max = mDividerDrawLast ? mDividersCount + 1 : mDividersCount;
                if ((dividerPositionHidden != null) && (dividerPositionHidden.length > 0)) {
                    int actualHiddenPosition = dividerPositionHidden[0];
                    int hiddenPositionIteratedPosition = 0;
                    for (; i < max; i++) {
                        if (actualHiddenPosition + actualPositionHiddenShift == i) {
                            actualHiddenPosition = hiddenPositionIteratedPosition + 1 < dividerPositionHidden.length ? dividerPositionHidden[++hiddenPositionIteratedPosition] : actualHiddenPosition;
                        } else {
                            foregroundCanvas.drawArc(mRect, mStartAngle + i * mDividerStepAngle, mDividerSize, false, mPaintDivider);
                        }
                    }
                } else {
                    for (; i < max; i++) {
                        foregroundCanvas.drawArc(mRect, mStartAngle + i * mDividerStepAngle, mDividerSize, false, mPaintDivider);
                    }
                }
            }
        }
    }

    private void prepareTexts() {
// STRINGS
        if ((stringList != null) && (stringShiftsList != null)) {
            for (int i = 0; i < stringList.length; i++) {


                if (i == stringShiftsList.length - 1) {
                    float startAngle = mStartAngle - stringShiftsList[i] + 80;

                    stringOvalRect.set(rectLeft + stringsSize + stringPadding, rectTop + stringPadding + stringsSize, rectRight - stringPadding - stringsSize, rectBottom - stringPadding - stringsSize);
                    mLastStringArc.addArc(stringOvalRect, startAngle, -mSweepAngle);
                    backgroundCanvas.drawTextOnPath(stringList[i], mLastStringArc, 0, 20, mPaintText);
                } else {
                    float startAngle = mStartAngle + stringShiftsList[i];
                    stringOvalRect.set(rectLeft + stringsSize / 4 + stringPadding / 4, rectTop + stringPadding / 4 + stringsSize / 4, rectRight - stringPadding / 4 - stringsSize / 4, rectBottom - stringPadding / 4 - stringsSize / 4);
                    mStringArc = new Path();
                    mStringArc.addArc(stringOvalRect, startAngle, mSweepAngle);
                    backgroundCanvas.drawTextOnPath(stringList[i], mStringArc, 0, 20, mPaintText);
                }
            }

        }
    }

    private void prepareScale() {
        // SCALE
        if ((showScale) && (mScaleSize > 0)) {
            mScalePaint.setStrokeWidth(mScaleWidth);

            mScalePaint.setColor(mScaleColor);
            mScalePaint.setShader(null);
            float step = mSweepAngle / mScaleCount;
            int startAngle = mStartAngle;
            for (int i = 0; i < mScaleCount; i++) {
                backgroundCanvas.drawArc(mScaleRect, startAngle, mScaleSize, false, mScalePaint);
                if ((i % 5) == 0) {
                    ++startAngle;
                    backgroundCanvas.drawArc(mScaleRect, startAngle, mScaleSize, false, mScalePaint);
                }
                startAngle += step;
            }
        }
    }

    private void prepareStaticOvals() {
        Timber.e("GAUGE: STATIC OVALS %s", size);

        if (background != null) {
            background.recycle();
        }

        //CENTRAL OVAL
        centralOvalPaint = new Paint();
        setStyleToPaint(centralOvalPaint);
        centralOvalPaint.setColor(mStrokeCentralColor);
        centralOvalPaint.setStrokeWidth(mStrokeCentralWidth);
        centralOvalPaint.setShader(null);
        mCentralOvalRect.set(rectLeft - mStrokeCentralWidth / 2 - mStrokeOuterWidth,
                rectTop - mStrokeCentralWidth / 2 - mStrokeOuterWidth,
                rectRight + mStrokeCentralWidth / 2 + mStrokeOuterWidth,
                rectBottom + mStrokeCentralWidth / 2 + mStrokeOuterWidth);


//        backgroundCanvas.drawArc(mCentralOvalRect, mStartAngle, mSweepAngle, false, mPaint);

        //OUTER OVAL
        outerOvalPaint = new Paint();
        setStyleToPaint(outerOvalPaint);
        outerOvalPaint.setColor(mStrokeOuterColor);
        outerOvalPaint.setStrokeWidth(mStrokeOuterWidth);
        mPaint.setShader(null);

        mOuterOvalRect.set(rectLeft - mStrokeCentralWidth - mStrokeOuterWidth,
                rectTop - mStrokeCentralWidth - mStrokeOuterWidth,
                rectRight + mStrokeCentralWidth + mStrokeOuterWidth,
                rectBottom + mStrokeCentralWidth + mStrokeOuterWidth);

        size = getWidth();
        if (((size > 0) && (redrawAll)) || ((size > 0) && (background == null))) {
            Timber.e("GAUGE: STATIC OVALS INNER %s", size);
            try {
                background = Bitmap.createBitmap((int) size, (int) size, Bitmap.Config.ARGB_8888);
                throw new OutOfMemoryError();
            } catch (OutOfMemoryError outOfMemoryError) {
//                try {
//                    int size2 = (int) (size/2);
//                    background = Bitmap.createBitmap((int) size2, (int) size2, Bitmap.Config.ARGB_8888);
//                } catch (OutOfMemoryError outOfMemoryError1) {
//                    int size3 = (int) (size/4);
//                    background = Bitmap.createBitmap((int) size3, (int) size3, Bitmap.Config.ARGB_8888);
//                }

            }
//            background = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888);

            backgroundCanvas = new Canvas(background);

//            backgroundCanvas.scale(100, 100);
            backgroundCanvas.drawArc(mCentralOvalRect, mStartAngle, mSweepAngle, false, centralOvalPaint);
            backgroundCanvas.drawArc(mOuterOvalRect, mStartAngle, mSweepAngle, false, outerOvalPaint);
        }
    }

    private void setStyleToPaint(Paint mPaint) {
        mPaint.setAntiAlias(true);
        if (!TextUtils.isEmpty(mStrokeCap)) {
            if (mStrokeCap.equals("BUTT"))
                mPaint.setStrokeCap(Paint.Cap.BUTT);
            else if (mStrokeCap.equals("ROUND"))
                mPaint.setStrokeCap(Paint.Cap.ROUND);
        } else
            mPaint.setStrokeCap(Paint.Cap.BUTT);
        mPaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        size = getWidth() < getHeight() ? getWidth() : getHeight();
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int dimension = widthMeasureSpec < heightMeasureSpec ? widthMeasureSpec : heightMeasureSpec;
        setMeasuredDimension(dimension, dimension);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (redrawAll) {
            init();
        }

        if ((background == null) || redrawAll) {
            prepareStaticOvals();
            prepareTexts();
            prepareScale();
        }
        if (background != null) {
            canvas.drawBitmap(background, 0, 0, null);
        }

        float padding = getmStrokeInnerWidth();
        size = getWidth() < getHeight() ? getWidth() : getHeight();
        float width = size - (2 * padding);
        float height = size - (2 * padding);
        //        float radius = (width > height ? width/2 : height/2);
        radius = (width < height ? width / 2 : height / 2);
        rectLeft = (getWidth() - (2 * padding)) / 2 - radius + padding + mStrokeCentralWidth + mStrokeOuterWidth;
        rectTop = (getHeight() - (2 * padding)) / 2 - radius + padding + mStrokeCentralWidth + mStrokeOuterWidth;
        rectRight = (getWidth() - (2 * padding)) / 2 - radius + padding + width - mStrokeCentralWidth - mStrokeOuterWidth;
        rectBottom = (getHeight() - (2 * padding)) / 2 - radius + padding + height - mStrokeCentralWidth - mStrokeOuterWidth;


        canvas.drawArc(mBaseOvalRect, mStartAngle, mSweepAngle, false, paintOvalInner);

        if (mPointSize > 0) {//if size of pointer is defined
            if (mPoint > mStartAngle + mPointSize / 2) {
                canvas.drawArc(mRect, mPoint - mPointSize / 2, mPointSize, false, paintProgress);
            } else { //to avoid excedding start/zero point
                canvas.drawArc(mRect, mPoint, mPointSize, false, paintProgress);
            }
        } else { //draw from start point to value point (long pointer)
            if (mValue == mStartValue) //use non-zero default value for start point (to avoid lack of pointer for start/zero value)
                canvas.drawArc(mRect, mStartAngle, DEFAULT_LONG_POINTER_SIZE, false, paintProgress);
            else
                canvas.drawArc(mRect, mStartAngle, mPoint - mStartAngle, false, paintProgress);
        }

        //Arrow - pointer
        if (showArrow) {
//                Timber.e("gauge rect", "onDraw: " + rectLeft + " " + rectTop + " " + rectRight + " " + rectBottom);

            radius = startX;
            float angle = (float) ((mStartAngle + ((mValue - mStartValue) * mPointAngle)) * Math.PI / 180);
            float stopX = (float) (startX + (radius * 0.6) * Math.cos(angle));
            float stopY = (float) (startY + (radius * 0.6) * Math.sin(angle));

            Timber.e("gauge rect onDraw: %s %s %s %s" , startX,stopX ,startY ,stopY);

            Timber.e("gauge rect after: %s %s %s %s" , startX ,stopX , startY , stopY);
            canvas.drawLine(startX, startY, stopX, stopY, arrowPaint);

        }

        if ((foreground == null) || redrawAll) {
            prepareDividers();
            prepareArrow();
        }
        if (foreground != null) {
            canvas.drawBitmap(foreground, 0, 0, null);
        }
        canvas.save();
        canvas.restore();
        redrawAll = false;

    }

    public void setValueAnimated(final int value) {
        if (runnable != null) {
            runnable.setStop(true);
        }

        int direction = 1;
        if (value < mValue) {
            direction = -1;
        } else if (value == mValue) {
            direction = 0;
        }

        final int finalDirection = direction;

        if (runnable != null) {
            runnable.setParams(finalDirection, value);
        } else {
            runnable = new MyRunnable(finalDirection, value);
        }

        Handler handler = getHandler();
        if (handler != null) {
            handler.post(runnable);
        }
    }

    public void setMaxValue() {
        mValue = mEndValue;
        mPoint = (int) (mStartAngle + (mValue - mStartValue) * mPointAngle);
        redrawAll = true;
        invalidate();
    }

    public int getValue() {
        return mValue;
    }

    public void setValue(int value) {
        Timber.e("GAUGE2 %s %s",this,  gaugeValue);
        mValue = value;
        mPoint = (int) (mStartAngle + (mValue - mStartValue) * mPointAngle);
        invalidate();
    }

    public boolean isShowArrow() {
        return showArrow;
    }

    public void setShowArrow(boolean showArrow) {
        this.showArrow = showArrow;
        redrawAll = true;
        invalidate();
    }

    @SuppressWarnings("unused")
    public float getmStrokeInnerWidth() {
        return mStrokeInnerWidth;
    }

    @SuppressWarnings("unused")
    public void setmStrokeInnerWidth(float mStrokeInnerWidth) {
        this.mStrokeInnerWidth = mStrokeInnerWidth;
        redrawAll = true;
    }

    @SuppressWarnings("unused")
    public float getmStrokeCentralWidth() {
        return mStrokeCentralWidth;
    }

    @SuppressWarnings("unused")
    public void setmStrokeCentralWidth(float mStrokeCentralWidth) {
        this.mStrokeCentralWidth = mStrokeCentralWidth;
        redrawAll = true;
    }

    @SuppressWarnings("unused")
    public float getmStrokeOuterWidth() {
        return mStrokeOuterWidth;
    }

    @SuppressWarnings("unused")
    public void setmStrokeOuterWidth(float mStrokeOuterWidth) {
        this.mStrokeOuterWidth = mStrokeOuterWidth;
        redrawAll = true;
    }

    @SuppressWarnings("unused")
    public int getmStrokeInnerColor() {
        return mStrokeInnerColor;
    }

    @SuppressWarnings("unused")
    public void setmStrokeInnerColor(int mStrokeInnerColor) {
        this.mStrokeInnerColor = mStrokeInnerColor;
        redrawAll = true;
    }

    @SuppressWarnings("unused")
    public int getmStrokeCentralColor() {
        return mStrokeCentralColor;
    }

    @SuppressWarnings("unused")
    public void setmStrokeCentralColor(int mStrokeCentralColor) {
        this.mStrokeCentralColor = mStrokeCentralColor;
        redrawAll = true;
    }

    @SuppressWarnings("unused")
    public int getmStrokeOuterColor() {
        return mStrokeOuterColor;
    }

    @SuppressWarnings("unused")
    public void setmStrokeOuterColor(int mStrokeOuterColor) {
        this.mStrokeOuterColor = mStrokeOuterColor;
        redrawAll = true;
    }

    @SuppressWarnings("unused")
    public String getStrokeCap() {
        return mStrokeCap;
    }

    public void setStrokeCap(String strokeCap) {
        mStrokeCap = strokeCap;
        redrawAll = true;
    }

    @SuppressWarnings("unused")
    public int getStartAngle() {
        return mStartAngle;
    }

    public void setStartAngle(int startAngle) {
        mStartAngle = startAngle;
        redrawAll = true;
    }

    @SuppressWarnings("unused")
    public int getSweepAngle() {
        return mSweepAngle;
    }

    public void setSweepAngle(int sweepAngle) {
        mSweepAngle = sweepAngle;
        redrawAll = true;
    }

    @SuppressWarnings("unused")
    public int getStartValue() {
        return mStartValue;
    }

    public void setStartValue(int startValue) {
        mStartValue = startValue;
        redrawAll = true;
    }

    @SuppressWarnings("unused")
    public int getEndValue() {
        return mEndValue;
    }

    public void setEndValue(int endValue) {
        mEndValue = endValue;
        redrawAll = true;
        invalidate();
    }

    @SuppressWarnings("unused")
    public int getPointSize() {
        return mPointSize;
    }

    public void setPointSize(int pointSize) {
        mPointSize = pointSize;
        redrawAll = true;
    }

    @SuppressWarnings("unused")
    public int getPointStartColor() {
        return mPointStartColor;
    }

    public void setPointStartColor(int pointStartColor) {
        mPointStartColor = pointStartColor;
        redrawAll = true;
    }

    @SuppressWarnings("unused")
    public int getPointEndColor() {
        return mPointEndColor;
    }

    public void setPointEndColor(int pointEndColor) {
        mPointEndColor = pointEndColor;
        redrawAll = true;
    }

    @SuppressWarnings("unused")
    public int getDividerColor() {
        return mDividerColor;
    }

    public void setDividerColor(int dividerColor) {
        mDividerColor = dividerColor;
        redrawAll = true;
    }

    @SuppressWarnings("unused")
    public boolean isDividerDrawFirst() {
        return mDividerDrawFirst;
    }

    public void setDividerDrawFirst(boolean dividerDrawFirst) {
        mDividerDrawFirst = dividerDrawFirst;
    }

    @SuppressWarnings("unused")
    public boolean isDividerDrawLast() {
        return mDividerDrawLast;
    }

    public void setDividerDrawLast(boolean dividerDrawLast) {
        mDividerDrawLast = dividerDrawLast;
        redrawAll = true;
    }

    /**
     * This method set strings in the arc
     *
     * @param gaugeStrings set 0 if you do not want them
     */
    public void setGaugeStrings(int gaugeStrings) {
        redrawAll = true;
        if (gaugeStrings == 0) {
            this.stringList = null;
        } else {
            this.stringList = getResources().getStringArray(gaugeStrings);
        }
    }

    /**
     * This method set shift to strings in the arc
     *
     * @param stringShifts set 0 if you do not want them
     */
    public void setStringShifts(int stringShifts) {
        redrawAll = true;
        this.stringShiftsList = getResources().getIntArray(stringShifts);
    }

    public void setDividerSize(int dividerSize) {
        redrawAll = true;
        if (dividerSize > 0) {
            mDividerSize = mSweepAngle / (Math.abs(mEndValue - mStartValue) / dividerSize);
            mDividersCount = 100 / dividerStep;
            mDividerStepAngle = mSweepAngle / mDividersCount;
        } else {
            mDividerSize = 0;
        }
    }

    public class MyRunnable implements Runnable {

        final int step = 1;
        int finalDirection = 0;
        int value = 0;
        boolean run = true;

        public MyRunnable(int finalDirection, int value) {
            this.finalDirection = finalDirection;
            this.value = value;
        }

        public void setStop(boolean stop) {
            this.run = !stop;
        }

        @Override
        public void run() {
            if (finalDirection > 0) {
                int i = (value - mValue) / 4;
                if (i < 1) {
                    i = 1;
                }
                mValue = mValue + finalDirection * step * i;
                if (mValue >= value) {
                    setValue(value);
                } else {
                    setValue(mValue);
                    if (run)
                        getHandler().postDelayed(this, 32);
                }
            } else if (finalDirection < 0) {
                int i = (mValue - value) / 4;
                if (i < 1) {
                    i = 1;
                }
                mValue = mValue + finalDirection * step * i;
                if (mValue <= value) {
                    setValue(value);
                } else {
                    setValue(mValue);
                    if (run)
                        getHandler().postDelayed(this, 32);
                }
            }
        }

        public void setParams(int finalDirection, int value) {
            this.finalDirection = finalDirection;
            this.value = value;
        }
    }
}
