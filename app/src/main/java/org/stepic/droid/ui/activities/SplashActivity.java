package org.stepic.droid.ui.activities;


import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.os.Handler;

import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.stepic.droid.R;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.notifications.StepicInstanceIdService;
import org.stepic.droid.util.AppConstants;

import java.io.IOException;
import java.util.Arrays;

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

        threadPoolExecutor.execute(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            shell.getApi().getUserProfile().execute(); // make this "ping" request for updating refresh tokens and log out user, if it is revoked.
                        } catch (IOException e) {
                            //ignore
                        }
                    }
                }
        );

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
            ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);

            String findCoursesLabel = getString(R.string.find_courses_title);
            Intent findCoursesIntent = shell.getScreenProvider().getShowFindCoursesIntent(getApplicationContext());
            findCoursesIntent.setAction(AppConstants.OPEN_SHORTCUT_FIND_COURSES);
            ShortcutInfo findCoursesShortcut = new ShortcutInfo.Builder(this, AppConstants.FIND_COURSES_SHORTCUT_ID)
                    .setShortLabel(findCoursesLabel)
                    .setLongLabel(findCoursesLabel)
                    .setIcon(Icon.createWithResource(this, R.mipmap.ic_search_icon_shortcut))
                    .setIntent(findCoursesIntent)
                    .build();

            String profileLabel = getString(R.string.profile_title);
            Intent mainFeedActivityIntent = shell.getScreenProvider().getMyCoursesIntent(getApplicationContext());
            mainFeedActivityIntent.setAction(AppConstants.OPEN_SHORTCUT_PROFILE);
            Intent profileIntent = shell.getScreenProvider().getProfileIntent(getApplicationContext());
            profileIntent.setAction(AppConstants.OPEN_SHORTCUT_PROFILE);
            ShortcutInfo profileShortcut = new ShortcutInfo.Builder(this, AppConstants.PROFILE_SHORTCUT_ID)
                    .setShortLabel(profileLabel)
                    .setLongLabel(profileLabel)
                    .setIcon(Icon.createWithResource(this, R.mipmap.ic_profile_shortcut))
                    .setIntents(new Intent[]{mainFeedActivityIntent, profileIntent})
                    .build();


            shortcutManager.setDynamicShortcuts(Arrays.asList(findCoursesShortcut, profileShortcut));
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
            MixpanelAPI mixpanelAPI = MixpanelAPI.getInstance(getApplicationContext(), config.getMixpanelToken());
            mixpanelAPI.track("app_opened");
            if (sharedPreferenceHelper.getAuthResponseFromStore() != null) {
                shell.getScreenProvider().showMainFeed(SplashActivity.this);
            } else {
                shell.getScreenProvider().showLaunchScreen(this);
            }
            finish();
        }
    }
}
