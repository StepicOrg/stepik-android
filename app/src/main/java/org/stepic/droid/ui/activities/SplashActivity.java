package org.stepic.droid.ui.activities;


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

        if (checkPlayServices() && !sharedPreferenceHelper.isGcmTokenOk()) {

            threadPoolExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    StepicInstanceIdService.Companion.updateAnywhere(shell.getApi(), sharedPreferenceHelper, analytic); //FCM!
                }
            });
        }

        if (sharedPreferenceHelper.isFirstTime() || !sharedPreferenceHelper.isScheduleAdded() || sharedPreferenceHelper.isNeedDropCoursesIn114()) {
            //fix v11 bug:
            threadPoolExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    if (sharedPreferenceHelper.isFirstTime()) {
                        databaseFacade.dropOnlyCourseTable(); //v11 bug, when slug was not cached. We can remove it, when all users will have v1.11 or above. (flavour problem)
                        sharedPreferenceHelper.afterFirstTime();
                        sharedPreferenceHelper.afterScheduleAdded();
                        sharedPreferenceHelper.afterNeedDropCoursesIn114();
                        defaultFilter.setNeedResolveLanguage(); //if user 1st time and v1.16 or more --> resolve language
                    } else if (!sharedPreferenceHelper.isScheduleAdded()) {
                        databaseFacade.dropOnlyCourseTable();
                        sharedPreferenceHelper.afterScheduleAdded();
                        sharedPreferenceHelper.afterNeedDropCoursesIn114();
                    } else if (sharedPreferenceHelper.isNeedDropCoursesIn114()){
                        databaseFacade.dropOnlyCourseTable();
                        sharedPreferenceHelper.afterNeedDropCoursesIn114();
                    }

                    mainHandler.post(new Function0<Unit>() {
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
            SharedPreferenceHelper helper = shell.getSharedPreferenceHelper();
            if (helper.getAuthResponseFromStore() != null) {
                shell.getScreenProvider().showMainFeed(SplashActivity.this);
            } else {
                shell.getScreenProvider().showLaunchScreen(SplashActivity.this, false);
            }
            finish();
        }
    }
}
