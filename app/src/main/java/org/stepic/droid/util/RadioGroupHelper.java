package org.stepic.droid.util;

import android.widget.RadioGroup;

import org.stepic.droid.ui.custom.StepicRadioGroup;

public class RadioGroupHelper {
    public static void setEnabled(RadioGroup testRadioGroup, boolean isEnabled) {
        for (int i = 0; i < testRadioGroup.getChildCount(); i++) {
            testRadioGroup.getChildAt(i).setEnabled(isEnabled);
        }
    }

    public static void setEnabled(StepicRadioGroup testRadioGroup, boolean isEnabled) {
        for (int i = 0; i < testRadioGroup.getChildCount(); i++) {
            testRadioGroup.getChildAt(i).setEnabled(isEnabled);
        }
    }
}
