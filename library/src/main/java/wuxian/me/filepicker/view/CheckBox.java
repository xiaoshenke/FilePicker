/*
 * This is the source code of Telegram for Android v. 3.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2016.
 */

package wuxian.me.filepicker.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

public class CheckBox extends View {

    private Drawable checkDrawable;
    private static Paint paint;
    private static Paint eraser;
    private static Paint eraser2;
    private static Paint checkPaint;
    private static Paint backgroundPaint;

    private Bitmap drawBitmap;
    private Bitmap checkBitmap;
    private Canvas bitmapCanvas;
    private Canvas checkCanvas;

    private boolean drawBackground;

    private float progress;
    private ObjectAnimator checkAnimator;
    private boolean isCheckAnimation = true;

    private boolean attachedToWindow;
    private boolean isChecked;

    private int size = 22;
    private int checkOffset;
    private int color = 0xff5ec245;

    private final static float progressBounceDiff = 0.2f;

    private Context context;

    public CheckBox(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public CheckBox(Context context, int resId) {
        super(context);

        this.context = context;
        init();

        setDrawableResource(resId);
    }

    public void setDrawableResource(int resId) {
        checkDrawable = context.getResources().getDrawable(resId);

        invalidate();
    }

    private void init() {
        if (paint == null) {
            paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            eraser = new Paint(Paint.ANTI_ALIAS_FLAG);
            eraser.setColor(0);
            eraser.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            eraser2 = new Paint(Paint.ANTI_ALIAS_FLAG);
            eraser2.setColor(0);
            eraser2.setStyle(Paint.Style.STROKE);
            eraser2.setStrokeWidth(Utils.dp(getContext(),28));
            eraser2.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            backgroundPaint.setColor(0xffffffff);
            backgroundPaint.setStyle(Paint.Style.STROKE);
            backgroundPaint.setStrokeWidth(Utils.dp(getContext(),2));
        }
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == VISIBLE && drawBitmap == null) {
            drawBitmap = Bitmap.createBitmap(Utils.dp(getContext(),size), Utils.dp(getContext(),size), Bitmap.Config.ARGB_4444);
            bitmapCanvas = new Canvas(drawBitmap);
            checkBitmap = Bitmap.createBitmap(Utils.dp(getContext(),size), Utils.dp(getContext(),size), Bitmap.Config.ARGB_4444);
            checkCanvas = new Canvas(checkBitmap);
        }
    }

    public void setProgress(float value) {
        if (progress == value) {
            return;
        }
        progress = value;
        invalidate();
    }

    public void setDrawBackground(boolean value) {
        drawBackground = value;
    }

    public void setCheckOffset(int value) {
        checkOffset = value;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public float getProgress() {
        return progress;
    }

    public void setColor(int value) {
        color = value;
    }

    private void cancelCheckAnimator() {
        if (checkAnimator != null) {
            checkAnimator.cancel();
        }
    }

    private void animateToCheckedState(boolean newCheckedState) {
        isCheckAnimation = newCheckedState;
        checkAnimator = ObjectAnimator.ofFloat(this, "progress", newCheckedState ? 1 : 0);
        checkAnimator.setDuration(50);
        checkAnimator.start();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        attachedToWindow = true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        attachedToWindow = false;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    public void setChecked(boolean checked, boolean animated) {
        if (checked == isChecked) {
            return;
        }
        isChecked = checked;

        if (attachedToWindow && animated) {
            animateToCheckedState(checked);
        } else {
            cancelCheckAnimator();
            setProgress(checked ? 1.0f : 0.0f);
        }
    }

    public boolean isChecked() {
        return isChecked;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (getVisibility() != VISIBLE) {
            return;
        }
        if (drawBackground || progress != 0) {
            eraser2.setStrokeWidth(Utils.dp(getContext(),size + 6));

            drawBitmap.eraseColor(0);
            float rad = getMeasuredWidth() / 2;

            float roundProgress = progress >= 0.5f ? 1.0f : progress / 0.5f;
            float checkProgress = progress < 0.5f ? 0.0f : (progress - 0.5f) / 0.5f;

            float roundProgressCheckState = isCheckAnimation ? progress : (1.0f - progress);
            if (roundProgressCheckState < progressBounceDiff) {
                rad -= Utils.dp(getContext(),2) * roundProgressCheckState / progressBounceDiff;
            } else if (roundProgressCheckState < progressBounceDiff * 2) {
                rad -= Utils.dp(getContext(),2) - Utils.dp(getContext(),2) * (roundProgressCheckState - progressBounceDiff) / progressBounceDiff;
            }
            if (drawBackground) {
                paint.setColor(0x44000000);
                canvas.drawCircle(getMeasuredWidth() / 2, getMeasuredHeight() / 2, rad - Utils.dp(getContext(),1), paint);
                canvas.drawCircle(getMeasuredWidth() / 2, getMeasuredHeight() / 2, rad - Utils.dp(getContext(),1), backgroundPaint);
            }

            paint.setColor(color);

            bitmapCanvas.drawCircle(getMeasuredWidth() / 2, getMeasuredHeight() / 2, rad, paint);
            bitmapCanvas.drawCircle(getMeasuredWidth() / 2, getMeasuredHeight() / 2, rad * (1 - roundProgress), eraser);
            canvas.drawBitmap(drawBitmap, 0, 0, null);

            checkBitmap.eraseColor(0);

            if (checkDrawable != null) {
                int w = checkDrawable.getIntrinsicWidth();
                int h = checkDrawable.getIntrinsicHeight();
                int x = (getMeasuredWidth() - w) / 2;
                int y = (getMeasuredHeight() - h) / 2;

                checkDrawable.setBounds(x, y + checkOffset, x + w, y + h + checkOffset);
                checkDrawable.draw(checkCanvas);
            }

            checkCanvas.drawCircle(getMeasuredWidth() / 2 - Utils.dp(getContext(),2.5f), getMeasuredHeight() / 2 + Utils.dp(getContext(),4), ((getMeasuredWidth() + Utils.dp(getContext(),6)) / 2) * (1 - checkProgress), eraser2);

            canvas.drawBitmap(checkBitmap, 0, 0, null);
        }
    }
}
