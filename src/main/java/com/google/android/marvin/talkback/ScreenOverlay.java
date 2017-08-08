package com.google.android.marvin.talkback;

/**
 * Easily draw highlight rectangles on screen.
 */
import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.content.Context;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import android.widget.TextView;


@SuppressLint("ViewConstructor")
class ScreenOverlay {

    private final WindowManager mWindowManager;

    private final AccessibilityService mService;

    private View mOverlayView = null;

    private static final int OVERLAY_DELAY_IN_SECONDS = 5;

    private static final int MILLISECONDS_IN_A_SECOND = 1000;

    ScreenOverlay(AccessibilityService instance) {

        mService = instance;

        mWindowManager = (WindowManager) mService.getSystemService(Context.WINDOW_SERVICE);

    }

    private static WindowManager.LayoutParams getLayoutParams(DisplayMetrics metrics) {

        WindowManager.LayoutParams topButtonParams = new WindowManager.LayoutParams(
                metrics.widthPixels,
                metrics.heightPixels,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.RGBA_8888);

        topButtonParams.alpha = (float) 1.0;
        topButtonParams.gravity = Gravity.CENTER;

        return topButtonParams;
    }

    private boolean canDrawOverlays() {
        //noinspection SimplifiableIfStatement
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(mService)) {
                return false;
            }
        }

        return !mProhibitOverlay;
    }

    void overlayScreen() {
        if (!canDrawOverlays()) return;

        if (mOverlayView != null) return;

        TextView textView = new TextView(mService);

        textView.setGravity(Gravity.CENTER);

        textView.setBackgroundColor(Color.BLACK);
        textView.setTextColor(Color.WHITE);
        final int padding = 50;
        textView.setPadding(padding, padding, padding, padding);
        textView.setTextSize(20);

        textView.setText("While this accessibility service is active this overlay will block all screen contents. Not even the Accessibility Focus rectangle will be visible. This forces you to rely on TalkBack spoken feedback the way a blind user would. \n\nIf you get stuck, quickly swiping DOWN and UP will disable the overlay for " +  OVERLAY_DELAY_IN_SECONDS  + " seconds. Try not to rely on this \"cheat\" too frequently. Use TalkBack feedback to re-orient yourself with your current view, the same way a blind user would! Good luck!");

        mWindowManager.addView(textView, getLayoutParams(mService.getResources().getDisplayMetrics()));

        mOverlayView = textView;

    }

    private boolean mProhibitOverlay = false;

    private final Handler mOverlayHandler = new Handler();

    private final Runnable mOverlayRunnable = new Runnable() {
        @Override
        public void run() {
            mProhibitOverlay = false;
            overlayScreen();
        }
    };

    void removeOverlay() {
        if (canDrawOverlays()) mWindowManager.removeView(mOverlayView);

        mOverlayView = null;

        mOverlayHandler.removeCallbacks(mOverlayRunnable);
    }

    void removeOverlayTemporarily() {
        removeOverlay();

        mProhibitOverlay = true;

        mOverlayHandler.postDelayed(mOverlayRunnable, OVERLAY_DELAY_IN_SECONDS * MILLISECONDS_IN_A_SECOND);
    }
}