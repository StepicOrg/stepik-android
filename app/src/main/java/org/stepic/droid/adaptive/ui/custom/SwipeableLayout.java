package org.stepic.droid.adaptive.ui.custom;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.adaptive.math.LinearRegression;
import org.stepic.droid.adaptive.ui.animations.CardAnimations;

import java.util.HashSet;
import java.util.Set;

public final class SwipeableLayout extends FrameLayout {
    private float startX = 0;
    private float startY = 0;

    private float elemX;
    private float elemY;

    private final int screenWidth;
    private final int screenHeight;
    private final float viewX;

    private final GestureDetector flingDetector;

    private final static float MIN_FLING_VELOCITY = 400;
    private final static float ROTATION_ANGLE = 15.5f;

    private final static float MIN_DELTA = Resources.getSystem().getDisplayMetrics().density * 16;

    private final static int TOUCH_ABOVE = 0;
    private final static int TOUCH_BELOW = 1;
    private int touchPosition;

    private boolean intercepted = false;

    private final float MIN_FLING_TRANSLATION;

    private Set<SwipeListener> listeners = new HashSet<>();

    public SwipeableLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.flingDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float vx, float vy) {
                onMotionEnd(vx, vy);
                return true;
            }
        });

        this.screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
        this.screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;

        this.viewX = getX();

        MIN_FLING_TRANSLATION = this.screenWidth / 4;
    }

    private CardScrollView nestedScroll;

    public void setNestedScroll(CardScrollView nestedScroll) {
        this.nestedScroll = nestedScroll;
    }

    @Override
    public boolean onInterceptTouchEvent(final MotionEvent motionEvent) {
        processTouchEvent(motionEvent, false);
        return intercepted || super.onInterceptTouchEvent(motionEvent);
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        processTouchEvent(motionEvent, true);
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN && isEnabled()) {
            return true;
        } else {
            return super.onTouchEvent(motionEvent);
        }
    }

    private void processTouchEvent(MotionEvent motionEvent, boolean isOwnEvent) {
        if (!this.isEnabled()) return;
        boolean isFling = flingDetector.onTouchEvent(motionEvent);
        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                startX = motionEvent.getX();
                startY = motionEvent.getY();

                elemX = getTranslationX();
                elemY = getTranslationY();

                if (startY < getHeight() / 2) {
                    touchPosition = TOUCH_ABOVE;
                } else {
                    touchPosition = TOUCH_BELOW;
                }

                getParent().requestDisallowInterceptTouchEvent(true);
                intercepted = false;
                break;

            case MotionEvent.ACTION_MOVE:
                float dx = motionEvent.getX() - startX;
                float dy = motionEvent.getY() - startY;

                float adx = Math.abs(dx);
                float ady = Math.abs(dy);

                intercepted =
                        intercepted ||
                        isOwnEvent ||
                        nestedScroll != null && !nestedScroll.canScrollVertically() && (ady > MIN_DELTA || adx > MIN_DELTA) ||
                        adx > MIN_DELTA && adx > ady;// || Math.abs(dy) > MIN_DELTA;

                if (intercepted) {
                    elemX += dx;
                    elemY += dy;

                    setTranslationX(elemX);
                    setTranslationY(elemY);

                    float rotation = ROTATION_ANGLE * 2 * (elemX - viewX) / getWidth();
                    if (touchPosition == TOUCH_BELOW) {
                        rotation = -rotation;
                    }
                    setRotation(rotation);

                    for (SwipeListener l : listeners) {
                        l.onScroll(elemX / MIN_FLING_TRANSLATION / 2);
                    }
                }
                break;

            case MotionEvent.ACTION_CANCEL: // same as ACTION_UP, but we reset card to initial position
                elemX = 0;
                elemY = 0;

                setTranslationX(elemX);
                setTranslationY(elemY);

            case MotionEvent.ACTION_UP:
                if (!isFling) {
                    onMotionEnd(0, 0);
                }
                getParent().requestDisallowInterceptTouchEvent(false);
                intercepted = false;
                break;
        }
    }

    private float getTargetY(final float targetX) {
        final LinearRegression regression = new LinearRegression(new double[]{0, elemX}, new double[]{0, elemY});
        return (float) regression.predict(targetX);
    }

    private void onMotionEnd(final float vx, final float vy) {
        if (Math.abs(elemX) > MIN_FLING_TRANSLATION) {
            if (elemX > 0) {
                for (SwipeListener l : listeners) {
                    l.onSwipeRight();
                }
            } else {
                for (SwipeListener l : listeners) {
                    l.onSwipeLeft();
                }
            }
            final float targetX = Math.signum(elemX) * screenWidth;
            final float targetY = getTargetY(targetX);
            CardAnimations.createTransitionAnimation(this, targetX, targetY)
                    .rotation(0)
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            for (SwipeListener l : listeners) {
                                l.onSwiped();
                            }
                        }
                    })
                    .start();
        } else {
            if (Math.abs(vy) > MIN_FLING_VELOCITY) {
                for (SwipeListener l : listeners) {
                    l.onFlingDown();
                }
            }

            for (SwipeListener l : listeners) {
                l.onScroll(0);
            }
            CardAnimations.playRollBackAnimation(this);
        }
    }

    public void swipeDown() {
        setEnabled(false);
        for (SwipeListener l : listeners) {
            l.onSwipeDown();
        }

        CardAnimations.createTransitionAnimation(this, 0, screenHeight)
                .rotation(0)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        for (SwipeListener l : listeners) {
                            l.onSwiped();
                        }
                    }
                })
                .start();
    }

    public void setSwipeListener(@NotNull final SwipeListener listener) {
        this.listeners.add(listener);
    }

    public static class SwipeListener {
        public void onSwiped() {}
        public void onFlingDown() {}
        public void onSwipeLeft() {}
        public void onSwipeRight() {}
        public void onSwipeDown() {}

        /**
         *
         * @param scrollProgress - represents scroll progress for current state, e.g. 1.0 when card completely swiped to right
         */
        public void onScroll(final float scrollProgress) {}

    }
}