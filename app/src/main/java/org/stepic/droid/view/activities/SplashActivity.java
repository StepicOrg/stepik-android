package org.stepic.droid.view.activities;


import android.os.Bundle;
import android.os.Handler;

import org.stepic.droid.preferences.SharedPreferenceHelper;

import butterknife.ButterKnife;


public class SplashActivity extends BackToExitActivityBase {

    // Splash screen wait time
    private static final int SPLASH_TIME_OUT = 1500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //This stops from opening again from the Splash screen when minimized
        if (!isTaskRoot()) {
            finish();
            return;
        }

        setContentView(org.stepic.droid.R.layout.activity_splash);
        ButterKnife.bind(this);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isFinishing()) {

                    SharedPreferenceHelper helper = mShell.getSharedPreferenceHelper();
                    if (helper.getAuthResponseFromStore() != null) {
                        mShell.getScreenProvider().showMainFeed(SplashActivity.this);
                    } else {
                        mShell.getScreenProvider().showLaunchScreen(SplashActivity.this, false);
                    }


                    finish();//it is obsolete todo: is it clear when use clear top
                }
            }
        }, SPLASH_TIME_OUT);

    }

    @Override
    protected void onStart() {
        super.onStart();
        bus.register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        bus.unregister(this);
    }

}
