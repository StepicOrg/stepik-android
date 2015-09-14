package com.elpatika.stepic.base;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import com.elpatika.stepic.R;
import com.elpatika.stepic.core.IShell;

import javax.inject.Inject;

public abstract class StepicBaseFragmentActivity extends AppCompatActivity {


    @Inject
    protected IShell mShell;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainApplication.component(this).inject(this);
    }

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
