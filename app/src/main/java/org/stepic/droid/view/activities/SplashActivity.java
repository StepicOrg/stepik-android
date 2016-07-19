package org.stepic.droid.view.activities;


import android.os.Bundle;
import android.os.Handler;

import com.squareup.otto.Subscribe;

import org.stepic.droid.events.FirstTimeActionIsDoneEvent;
import org.stepic.droid.notifications.StepicInstanceIdService;
import org.stepic.droid.preferences.SharedPreferenceHelper;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;


public class SplashActivity extends BackToExitActivityBase {

    // Splash screen wait time
    private static final int SPLASH_TIME_OUT = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bus.register(this);

        //This stops from opening again from the Splash screen when minimized
        if (!isTaskRoot()) {
            finish();
            return;
        }

        if (checkPlayServices() && !mSharedPreferenceHelper.isGcmTokenOk()) {

            mThreadPoolExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    StepicInstanceIdService.Companion.updateAnywhere(mShell.getApi(), mSharedPreferenceHelper, analytic); //FCM!
                }
            });
        }

        if (mSharedPreferenceHelper.isFirstTime() || !mSharedPreferenceHelper.isScheduleAdded()) {
            //fix v11 bug:
            mThreadPoolExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    if (mSharedPreferenceHelper.isFirstTime()) {
                        mDbManager.dropOnlyCourseTable(); //v11 bug, when slug was not cached. We can remove it, when all users will have v1.11 or above. (flavour problem)
                        mSharedPreferenceHelper.afterFirstTime();
                        mSharedPreferenceHelper.afterScheduleAdded();
                    } else if (!mSharedPreferenceHelper.isScheduleAdded()) {
                        mDbManager.dropOnlyCourseTable();
                        mSharedPreferenceHelper.afterScheduleAdded();
                    }

                    mMainHandler.post(new Function0<Unit>() {
                        @Override
                        public Unit invoke() {
                            bus.post(new FirstTimeActionIsDoneEvent());
                            return Unit.INSTANCE;
                        }
                    });

                }
            });

        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    showNextScreen();
                }
            }, SPLASH_TIME_OUT);
        }


    }

    @Subscribe
    public void onFirstTimeActionsWasDone(FirstTimeActionIsDoneEvent event) {
        showNextScreen();
    }


    @Override
    protected void onDestroy() {
        bus.unregister(this);
        super.onDestroy();
    }

    private void showNextScreen() {
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
}
