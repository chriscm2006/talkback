package com.google.android.marvin.talkback;

/**
 * Easily draw highlight rectangles on screen.
 */
    import android.annotation.SuppressLint;
    import android.graphics.PixelFormat;
    import android.os.Build;
    import android.provider.Settings;
    import android.content.Context;
    import android.graphics.Color;
    import android.util.Log;
    import android.view.Gravity;
    import android.view.View;
    import android.view.WindowManager;
    import android.view.accessibility.AccessibilityNodeInfo;
    import android.widget.FrameLayout;
    import android.widget.RelativeLayout;
    import android.graphics.Canvas;
    import android.graphics.Paint;
    import android.graphics.PorterDuff;
    import android.graphics.PorterDuffXfermode;
    import android.graphics.Rect;
    import android.widget.ImageView;


@SuppressLint("ViewConstructor")
class ScreenOverlay extends FrameLayout {

    private final WindowManager mWindowManager;


    ScreenOverlay(Context instance) {
        super(instance);

        mWindowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);

        if (canDrawOverlays()) {

            WindowManager.LayoutParams topButtonParams = new WindowManager.LayoutParams(
                    10000,
                    10000,
                    WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.RGBA_8888);

            topButtonParams.alpha = (float) 1.0;

            this.setLayoutParams(topButtonParams);

            try {
                mWindowManager.addView(this, topButtonParams);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean canDrawOverlays() {
        //noinspection SimplifiableIfStatement
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(getContext())) {
                Log.wtf("CHRIS", "Can't draw overlays");
                return false;
            } else {
                return true;
            }
        }

        return true;
    }

    void overlayScreen() {
        if (!canDrawOverlays()) return;

        Rect rect = new Rect();
        rect.top = 0;
        rect.left = 0;
        rect.right = 3000;
        rect.bottom = 3000;

        View view = new HollowRectangle(rect, Color.BLACK);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT,
                Gravity.CENTER);

        addView(view, params);
    }

    void highlightRectangle(Rect rect, int color) {
        if (!canDrawOverlays()) return;


    }

    private void highlightView(final View viewToHighlight, final View overlayView) {

        if (!canDrawOverlays()) return;

        LayoutParams result = new LayoutParams(this.getLayoutParams());

        int[] location = new int[2];
        int[] locationOfView = new int[2];
        int[] locationOfTopButton = new int[2];

        viewToHighlight.getLocationOnScreen(locationOfView);
        getLocationOnScreen(locationOfTopButton);

        location[0] = locationOfView[0] - locationOfTopButton[0];
        location[1] = locationOfView[1] - locationOfTopButton[1];

        result.setMargins(0, 0, 0, 0);
        result.leftMargin = location[0];
        result.topMargin = location[1];
        result.height = viewToHighlight.getHeight();
        result.width = viewToHighlight.getWidth();

        addView(overlayView, result);
    }

    void detach(){
        removeAllViews();
        if (canDrawOverlays()) mWindowManager.removeView(this);
    }

    @SuppressLint("AppCompatCustomView")
    private class HollowRectangle extends ImageView {

        private final Rect OUTER_RECTANGLE;

        private final Paint mOuterPaint;

        private HollowRectangle(Rect rect, int color) {
            this(rect.width(), rect.height(), color);
        }

        private HollowRectangle(int width, int height, int color) {
            super(ScreenOverlay.this.getContext());

            OUTER_RECTANGLE = new Rect(0, 0, width, height);

            mOuterPaint = new Paint();
            mOuterPaint.setColor(color);
            mOuterPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        }

        protected void onDraw(Canvas canvas) {
            canvas.drawRect(OUTER_RECTANGLE, mOuterPaint);
        }
    }
}