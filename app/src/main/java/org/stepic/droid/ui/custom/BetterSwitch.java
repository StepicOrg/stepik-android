package org.stepic.droid.ui.custom;
import android.content.Context;
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class BetterSwitch extends SwitchCompat {
    //Constructors here...

    private boolean mUserTriggered;

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
        return mUserTriggered;
    }

    // Override this method to handle the case where user drags the switch
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        boolean result;

        mUserTriggered = true;
        result = super.onTouchEvent(ev);
        mUserTriggered = false;

        return result;
    }

    // Override this method to handle the case where user clicks the switch
    @Override
    public boolean performClick() {
        boolean result;

        mUserTriggered = true;
        result = super.performClick();
        mUserTriggered = false;

        return result;
    }
}