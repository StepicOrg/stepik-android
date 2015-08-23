package com.elpatika.stepic.base;

import android.view.WindowManager;

import com.elpatika.stepic.R;
import com.elpatika.stepic.core.IShell;
import com.google.inject.Inject;

import roboguice.RoboGuice;
import roboguice.activity.RoboFragmentActivity;

/**
 * Created by kirillmakarov on 23.08.15.
 */
public abstract class BaseFragmentActivity extends RoboFragmentActivity {


    @Inject
    protected IShell mShell;

    protected void hideSoftKeypad() {
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    @Override
    public void finish() {
        super.finish();
        applyTransitionPrev();
    }


    private void applyTransitionPrev() {
        // apply slide transition animation
        overridePendingTransition(R.anim.slide_in_from_start, R.anim.slide_out_to_end);
    }

}
