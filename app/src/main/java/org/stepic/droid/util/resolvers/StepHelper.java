package org.stepic.droid.util.resolvers;

import org.stepik.android.model.structure.Step;
import org.stepic.droid.util.AppConstants;

public class StepHelper {
    public static boolean isViewedStatePost(Step step) {
        if (step == null
                || step.getBlock() == null
                || step.getBlock().getName() == null
                || step.getBlock().getName().equals(""))
            return false;

        String type = step.getBlock().getName();
        switch (type) {
            case AppConstants.TYPE_VIDEO:
                return true;
            case AppConstants.TYPE_TEXT:
                return true;
            default:
                return false;
        }
    }
}
