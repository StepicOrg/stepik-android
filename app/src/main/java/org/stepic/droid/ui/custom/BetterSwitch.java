package org.stepic.droid.ui.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.appcompat.widget.SwitchCompat;

public class BetterSwitch extends SwitchCompat {
    //Constructors here...

    private boolean userTriggered;

    public BetterSwitch(Context context) {
        super(context);
    }

    public BetterSwitch(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BetterSwitch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // Use it in listener to check that listener is triggered by the user.
    public boolean isUserTriggered() {
        return userTriggered;
    }

    // Override this method to handle the case where user drags the switch
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        boolean result;

        userTriggered = true;
        result = super.onTouchEvent(ev);
        userTriggered = false;

        return result;
    }

    // Override this method to handle the case where user clicks the switch
    @Override
    public boolean performClick() {
        boolean result;

        userTriggered = true;
        result = super.performClick();
        userTriggered = false;

        return result;
    }
}