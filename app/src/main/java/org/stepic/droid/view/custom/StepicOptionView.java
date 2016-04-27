package org.stepic.droid.view.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.FrameLayout;

public abstract class StepicOptionView extends FrameLayout implements Checkable {
    public StepicOptionView(Context context) {
        super(context);
    }

    public StepicOptionView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StepicOptionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setChecked(boolean checked) {

    }

    @Override
    public boolean isChecked() {
        return false;
    }

    @Override
    public void toggle() {

    }

    public void setText(CharSequence text) {

    }
}
