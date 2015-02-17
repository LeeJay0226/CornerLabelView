package com.github.cornerlabelview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.IntDef;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by leejay on 2015/01/11.
 */
public class CornerLabelView2 extends View {

    private float mDegrees = 45f;
    private int mDistance = 20;
    private int mThickness = 20;
    private int mPivotH;
    private String mText;
    private TextPaint mTextPaint;
    private Paint mPaint;
    private Rect mMaxTextRect;
    private RectF mRectF;
    private float mTextX;
    private float mTextY;
    private StaticLayout mStaticLayout;

    @IntDef({LEFT_TOP, RIGHT_TOP})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Gravity {
    }

    private int mGravity;
    public static final int LEFT_TOP = 0;
    public static final int RIGHT_TOP = 1;

    public CornerLabelView2(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mTextPaint = new TextPaint();
        mTextPaint.setAntiAlias(true);
        mMaxTextRect = new Rect();
        mRectF = new RectF();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        DisplayMetrics dm = getResources().getDisplayMetrics();

        mDistance = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mDistance, dm);
        mThickness = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mThickness, dm);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CornerLabelView, defStyle, 0);
        if (a != null) {
            mDegrees = a.getFloat(R.styleable.CornerLabelView_clv_degrees, mDegrees) % 90;
            if (mDegrees <= 0) {
                mDegrees = 30f;
            }
            mThickness = a.getDimensionPixelSize(R.styleable.CornerLabelView_clv_thickness, mThickness);
            mDistance = a.getDimensionPixelSize(R.styleable.CornerLabelView_clv_distance, mThickness);
            mTextPaint.setColor(a.getColor(R.styleable.CornerLabelView_clv_textColor, Color.WHITE));
            setText(a.getString(R.styleable.CornerLabelView_clv_text));
            mGravity = a.getInt(R.styleable.CornerLabelView_clv_gravity, RIGHT_TOP);
            a.recycle();
        }
    }

    public CornerLabelView2(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CornerLabelView2(Context context) {
        this(context, null);
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.saveLayerAlpha(mRectF, 255, Canvas.HAS_ALPHA_LAYER_SAVE_FLAG);
        if (mGravity == LEFT_TOP) {
            canvas.rotate(-mDegrees, 0, mPivotH);
        } else {
            canvas.rotate(mDegrees, getMeasuredWidth(), mPivotH);
        }
        super.draw(canvas);
        canvas.drawRect(0, 0, getMeasuredWidth(), getMeasuredHeight() - mThickness, mPaint);
        // for 2.x android
        canvas.drawRect(-canvas.getWidth(), getMeasuredHeight(), canvas.getWidth(), canvas.getHeight(), mPaint);
        canvas.drawRect(getMeasuredWidth(), 0, canvas.getWidth(), canvas.getHeight(), mPaint);
        canvas.restore();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (null != mStaticLayout) {
            canvas.save();
            canvas.translate(mTextX, mTextY);
            mStaticLayout.draw(canvas);
            canvas.restore();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        double cosValue = Math.cos(Math.toRadians(mDegrees));
        double sinValue = Math.sin(Math.toRadians(mDegrees));
        double tanValue = Math.tan(Math.toRadians(mDegrees));
        float h1 = (float) (mDistance / cosValue);
        float h2 = (float) (mThickness / cosValue);
        mPivotH = (int) (h1 + h2);
        int height = (int) (h1 + h2);
        int width = (int) (height / sinValue);

        int left, right;
        if (mGravity == LEFT_TOP) {
            left = (int) (mThickness * tanValue);
            right = width - (int) (mThickness / tanValue);
        } else {
            left = (int) (mThickness / tanValue);
            right = width - (int) (mThickness * tanValue);
        }
        int top = height - mThickness + getPaddingTop();
        int bottom = height - getPaddingBottom();
        mMaxTextRect.set(left, top, right, bottom);
        mRectF.set(0, 0, width, height);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        float trySize = mThickness * 0.6f;
        if (!TextUtils.isEmpty(mText)) {
            do {
                mTextPaint.setTextSize(trySize);
                mStaticLayout = new StaticLayout(mText, mTextPaint, mMaxTextRect.width(), Layout.Alignment.ALIGN_CENTER, 1.0F, 0.0F, false);

                mTextX = mMaxTextRect.left + (mMaxTextRect.width() - mStaticLayout.getWidth()) / 2;
                mTextY = mMaxTextRect.top + (mMaxTextRect.height() - mStaticLayout.getHeight()) / 2;
                trySize--;
            } while (mStaticLayout.getHeight() > mMaxTextRect.height());
        }
    }

    public void setText(String text) {
        if (!TextUtils.equals(mText, text)) {
            mText = text;
            requestLayout();
            invalidate();
        }
    }

    public String getText() {
        return mText;
    }

    public void setGravity(@Gravity int gravity) {
        if (this.mGravity != gravity) {
            this.mGravity = gravity;
            requestLayout();
            invalidate();
        }
    }

    public int getGravity() {
        return mGravity;
    }

    public void setDegrees(float degrees) {
        if (degrees > 0) {
            this.mDegrees = degrees % 90;
            requestLayout();
            invalidate();
        }
    }

    public float getDegrees() {
        return mDegrees;
    }

    public void setDistance(int distance) {
        if (mDistance != distance) {
            this.mDistance = distance;
            requestLayout();
            invalidate();
        }
    }

    public int getDistance() {
        return mDistance;
    }

    public void setThickness(int thickness) {
        if (this.mThickness != thickness) {
            this.mThickness = thickness;
            requestLayout();
            invalidate();
        }
    }

    public int getThickness() {
        return mThickness;
    }
}
