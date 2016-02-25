package org.stepic.droid.util;

import org.stepic.droid.model.IProgressable;

import java.util.ArrayList;
import java.util.List;

public class ProgressUtil {
    public static String[] getAllProgresses(List<? extends IProgressable> objects) {

        List<String> progressesId = new ArrayList<>();
        for (IProgressable item : objects) {
            progressesId.add(item.getProgressId());
        }
        return progressesId.toArray(new String[progressesId.size()]);
    }
}
