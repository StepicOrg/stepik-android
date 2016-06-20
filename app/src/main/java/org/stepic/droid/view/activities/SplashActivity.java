package org.stepic.droid.view.activities;


import android.os.Bundle;
import android.os.Handler;

import org.stepic.droid.notifications.StepicInstanceIdService;
import org.stepic.droid.preferences.SharedPreferenceHelper;


public class SplashActivity extends BackToExitActivityBase {

    // Splash screen wait time
    private static final int SPLASH_TIME_OUT = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //This stops from opening again from the Splash screen when minimized
        if (!isTaskRoot()) {
            finish();
            return;
        }

        if (checkPlayServices() && !mSharedPreferenceHelper.isGcmTokenOk()) {

            mThreadPoolExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    StepicInstanceIdService.Companion.updateAnywhere(mShell.getApi(), mSharedPreferenceHelper); //FU!
                }
            });
        }

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
                    finish();
                }
            }
        }, SPLASH_TIME_OUT);


    }
}
