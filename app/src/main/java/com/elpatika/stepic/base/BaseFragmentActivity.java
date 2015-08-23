package com.elpatika.stepic.base;

import android.view.WindowManager;

import com.elpatika.stepic.core.IShell;
import com.google.inject.Inject;

import roboguice.RoboGuice;
import roboguice.activity.RoboFragmentActivity;

/**
 * Created by kirillmakarov on 23.08.15.
 */
public class BaseFragmentActivity extends RoboFragmentActivity {


    @Inject
    protected IShell mShell;

    protected void hideSoftKeypad() {
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }
}
