package org.stepic.droid.adaptive.ui.animations;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.OvershootInterpolator;

public final class CardAnimations {
    public static final long ANIMATION_DURATION = 200;

    private static final OvershootInterpolator OvershootInterpolator2F = new OvershootInterpolator(2f);

    public static void playWiggleAnimation(final View view) {
        final ObjectAnimator animator = ObjectAnimator.ofFloat(view, View.TRANSLATION_X, 0, 10);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setDuration(ANIMATION_DURATION / 2);
        animator.setRepeatCount(5);
        animator.start();
    }

    public static SupportViewPropertyAnimator createTransitionAnimation(final View view, final float x, final float y) {
        return new SupportViewPropertyAnimator(view).setDuration(ANIMATION_DURATION)
                .translationX(x)
                .translationY(y);
    }

    public static void playRollBackAnimation(final View view) {
        createTransitionAnimation(view, 0, 0)
                .rotation(0)
                .setInterpolator(OvershootInterpolator2F)
                .start();
    }
}
