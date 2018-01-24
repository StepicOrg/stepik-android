package org.stepic.droid.ui.custom;

import android.content.Context;
import android.util.AttributeSet;

import org.stepic.droid.R;

public class StepikCheckBox extends StepikOptionView {
    public StepikCheckBox(Context context) {
        super(context, null);
    }

    public StepikCheckBox(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public StepikCheckBox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public int getCheckedDrawableForOption() {
        return R.drawable.ic_check_box_filled;
    }

    @Override
    public int getUncheckedDrawableForOption() {
        return R.drawable.ic_check_box;
    }
}
