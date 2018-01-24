package org.stepic.droid.ui.custom;

import android.content.Context;
import android.util.AttributeSet;

import org.stepic.droid.R;

public class StepikRadioButton extends StepikOptionView {
    public StepikRadioButton(Context context) {
        super(context, null);
    }

    public StepikRadioButton(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public StepikRadioButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void toggle() {
        if (!isChecked()) {
            super.toggle();
        }
    }

    @Override
    public int getCheckedDrawableForOption() {
        return R.drawable.ic_radio_button_filled;
    }

    @Override
    public int getUncheckedDrawableForOption() {
        return R.drawable.ic_radio_button;
    }
}
