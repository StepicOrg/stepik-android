package org.stepic.droid.ui.activities;


import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
    private static final int SPLASH_TIME_OUT = 500;

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
                            api.getUserProfile().execute(); // make this "ping" request for updating refresh tokens and log out user, if it is revoked.
                        } catch (IOException e) {
                            //ignore
                        }
                    }
                }
        );

        defineShortcuts();


        if (checkPlayServices() && !sharedPreferenceHelper.isGcmTokenOk()) {
            threadPoolExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    StepicInstanceIdService.Companion.updateAnywhere(api, sharedPreferenceHelper, analytic); //FCM!
                }
            });
        }

        if (savedInstanceState == null) {
            threadPoolExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    int numberOfLaunches = sharedPreferenceHelper.incrementNumberOfLaunches();
                    //after first increment it is 0, because of default value is -1.
                    if (numberOfLaunches <= 0) {
                        analytic.reportEvent(Analytic.System.FIRST_LAUNCH_AFTER_INSTALL);
                    }
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
                            checkRemoteConfigs();
                            return Unit.INSTANCE;
                        }
                    });

                }
            });

        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    checkRemoteConfigs();
                }
            }, SPLASH_TIME_OUT);
        }

    }

    private void defineShortcuts() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
            ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);

            String findCoursesLabel = getString(R.string.find_courses_title);
            Intent findCoursesIntent = screenManager.getShowFindCoursesIntent(getApplicationContext());
            findCoursesIntent.setAction(AppConstants.OPEN_SHORTCUT_FIND_COURSES);
            ShortcutInfo findCoursesShortcut = new ShortcutInfo.Builder(this, AppConstants.FIND_COURSES_SHORTCUT_ID)
                    .setShortLabel(findCoursesLabel)
                    .setLongLabel(findCoursesLabel)
                    .setIcon(Icon.createWithResource(this, R.mipmap.ic_search_icon_shortcut))
                    .setIntent(findCoursesIntent)
                    .build();

            String profileLabel = getString(R.string.profile_title);
            Intent mainFeedActivityIntent = screenManager.getMyCoursesIntent(getApplicationContext());
            mainFeedActivityIntent.setAction(AppConstants.OPEN_SHORTCUT_PROFILE);
            Intent profileIntent = screenManager.getProfileIntent(getApplicationContext());
            profileIntent.setAction(AppConstants.OPEN_SHORTCUT_PROFILE);
            ShortcutInfo profileShortcut = new ShortcutInfo.Builder(this, AppConstants.PROFILE_SHORTCUT_ID)
                    .setShortLabel(profileLabel)
                    .setLongLabel(profileLabel)
                    .setIcon(Icon.createWithResource(this, R.mipmap.ic_profile_shortcut))
                    .setIntents(new Intent[]{mainFeedActivityIntent, profileIntent})
                    .build();


            shortcutManager.setDynamicShortcuts(Arrays.asList(findCoursesShortcut, profileShortcut));
        }
    }

    private void checkRemoteConfigs() {
        if (!isFinishing()) {
            MixpanelAPI mixpanelAPI = MixpanelAPI.getInstance(getApplicationContext(), config.getMixpanelToken());
            mixpanelAPI.track("app_opened");
            if (checkPlayServices()) {
                firebaseRemoteConfig.fetch().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            analytic.reportEvent(Analytic.RemoteConfig.FETCHED_SUCCESSFUL);
                            firebaseRemoteConfig.activateFetched();
                        } else {
                            analytic.reportEvent(Analytic.RemoteConfig.FETCHED_UNSUCCESSFUL);
                        }
                    }
                });
                //do not wait fetch, because fail of it may be about 3 mins. User can't wait for it!
                showNextScreen();
            } else {
                showNextScreen();
            }

        }
    }

    private void showNextScreen() {
        if (!isFinishing()) {
            if (sharedPreferenceHelper.getAuthResponseFromStore() != null) {
                screenManager.showMainFeed(SplashActivity.this);
            } else {
                screenManager.showLaunchScreen(this);
            }
            finish();
        }
    }
}
