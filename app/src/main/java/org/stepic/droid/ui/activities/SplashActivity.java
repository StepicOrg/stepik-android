package org.stepic.droid.ui.activities;


import android.os.Bundle;
import android.os.Handler;

import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.notifications.StepicInstanceIdService;
import org.stepic.droid.preferences.SharedPreferenceHelper;
import org.stepic.droid.util.AppConstants;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;


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

        if (checkPlayServices() && !sharedPreferenceHelper.isGcmTokenOk()) {

            threadPoolExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    StepicInstanceIdService.Companion.updateAnywhere(shell.getApi(), sharedPreferenceHelper, analytic); //FCM!
                }
            });
        }

        if (savedInstanceState == null) {
            threadPoolExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    int numberOfLaunches = sharedPreferenceHelper.incrementNumberOfLaunches();
                    if (numberOfLaunches < AppConstants.LAUNCHES_FOR_EXPERT_USER) {
                        analytic.reportEvent(Analytic.Interaction.START_SPLASH, numberOfLaunches + "");
                    } else {
                        analytic.reportEvent(Analytic.Interaction.START_SPLASH_EXPERT, numberOfLaunches + "");
                    }
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
                    } else if (sharedPreferenceHelper.isNeedDropCoursesIn114()) {
                        databaseFacade.dropOnlyCourseTable();
                        sharedPreferenceHelper.afterNeedDropCoursesIn114();
                    }

                    mainHandler.post(new Function0<Unit>() {
                        @Override
                        public Unit invoke() {
                            showNextScreen();
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
