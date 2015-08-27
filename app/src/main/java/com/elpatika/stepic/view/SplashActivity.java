package com.elpatika.stepic.view;


import android.os.Bundle;
import android.os.Handler;

import com.elpatika.stepic.R;
import com.elpatika.stepic.base.BaseFragmentActivity;


public class SplashActivity extends BaseFragmentActivity {

    // Splash screen wait time
    private static final int SPLASH_TIME_OUT = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //This stops from opening again from the Splash screen when minimized
        if (!isTaskRoot()) {
            finish();
            return;
        }

        setContentView(R.layout.activity_splash);
        overridePendingTransition(R.anim.slide_in_from_end, R.anim.slide_out_to_start);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mShell.getScreenProvider().showLaunchScreen(SplashActivity.this, false);

                finish();
            }
        }, SPLASH_TIME_OUT);

    }
}
