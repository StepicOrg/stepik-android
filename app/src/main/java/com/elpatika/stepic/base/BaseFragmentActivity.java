package com.elpatika.stepic.base;

import android.view.WindowManager;

import roboguice.RoboGuice;
import roboguice.activity.RoboFragmentActivity;

/**
 * Created by kirillmakarov on 23.08.15.
 */
public class BaseFragmentActivity extends RoboFragmentActivity {

    static {
        RoboGuice.setUseAnnotationDatabases(false);
    }

    protected void hideSoftKeypad() {
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }
}
