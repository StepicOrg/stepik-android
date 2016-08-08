package org.stepic.droid.ui.custom;

import android.content.Context;
import android.util.AttributeSet;

import org.stepic.droid.R;

public class StepicCheckBox extends StepicOptionView {
    public StepicCheckBox(Context context) {
        super(context, null);
    }

    public StepicCheckBox(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public StepicCheckBox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public int getCheckedDrawableForOption() {
        return R.drawable.ic_check_box_white_24px;
    }

    @Override
    public int getUncheckedDrawableForOption() {
        return R.drawable.ic_check_box_outline_blank_white_24px;
    }
}
