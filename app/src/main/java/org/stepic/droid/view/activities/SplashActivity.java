package org.stepic.droid.view.activities;


import android.os.Bundle;
import android.os.Handler;

import org.stepic.droid.base.StepicBaseFragmentActivity;

import butterknife.ButterKnife;


public class SplashActivity extends StepicBaseFragmentActivity {

    // Splash screen wait time
    private static final int SPLASH_TIME_OUT = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);

        //This stops from opening again from the Splash screen when minimized
        if (!isTaskRoot()) {
            finish();
            return;
        }

        setContentView(org.stepic.droid.R.layout.activity_splash);
        overridePendingTransition(org.stepic.droid.R.anim.slide_in_from_end, org.stepic.droid.R.anim.slide_out_to_start);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isFinishing()) {
                    mShell.getScreenProvider().showLaunchScreen(SplashActivity.this, false);

                    finish();
                }
            }
        }, SPLASH_TIME_OUT);

    }
}
