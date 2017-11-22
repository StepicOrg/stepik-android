package org.stepic.droid.ui.activities;


import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Bundle;

import org.stepic.droid.R;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.base.App;
import org.stepic.droid.core.presenters.SplashPresenter;
import org.stepic.droid.core.presenters.contracts.SplashView;
import org.stepic.droid.notifications.StepicInstanceIdService;
import org.stepic.droid.util.AppConstants;

import java.util.Arrays;

import javax.inject.Inject;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;


public class SplashActivity extends BackToExitActivityBase implements SplashView {

    @Inject
    SplashPresenter splashPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //This stops from opening again from the Splash screen when minimized
        if (!isTaskRoot()) {
            finish();
            return;
        }
        App.Companion.componentManager().splashComponent().inject(this);
        splashPresenter.attachView(this);
        splashPresenter.onSplashCreated();

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
            checkRemoteConfigs();
        }

    }

    private void defineShortcuts() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
            ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);

            String catalogLabel = getString(R.string.catalog_title);
            Intent catalogIntent = screenManager.getCatalogIntent(getApplicationContext());
            catalogIntent.setAction(AppConstants.OPEN_SHORTCUT_CATALOG);
            ShortcutInfo catalogShortcut = new ShortcutInfo.Builder(this, AppConstants.CATALOG_SHORTCUT_ID)
                    .setShortLabel(catalogLabel)
                    .setLongLabel(catalogLabel)
                    .setIcon(Icon.createWithResource(this, R.drawable.ic_shortcut_find_courses))
                    .setIntent(catalogIntent)
                    .build();

            String profileLabel = getString(R.string.profile_title);
            Intent profileIntent = screenManager.getMyProfileIntent(getApplicationContext());
            profileIntent.setAction(AppConstants.OPEN_SHORTCUT_PROFILE);
            ShortcutInfo profileShortcut = new ShortcutInfo.Builder(this, AppConstants.PROFILE_SHORTCUT_ID)
                    .setShortLabel(profileLabel)
                    .setLongLabel(profileLabel)
                    .setIcon(Icon.createWithResource(this, R.drawable.ic_shortcut_profile))
                    .setIntent(profileIntent)
                    .build();


            shortcutManager.setDynamicShortcuts(Arrays.asList(catalogShortcut, profileShortcut));
        }
    }

    private void checkRemoteConfigs() {
        //remove
    }

    private void showNextScreen() {
        if (!isFinishing()) {
            if (sharedPreferenceHelper.getAuthResponseFromStore() != null) {
                screenManager.showMainFeedFromSplash(SplashActivity.this);
            } else {
                screenManager.showLaunchFromSplash(this);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        splashPresenter.detachView(this);
        if (isFinishing()) {
            App.Companion.componentManager().releaseSplashComponent();
        }
    }

    @Override
    public void onShowLaunch() {
        if (!isFinishing()) {
            screenManager.showLaunchFromSplash(this);
        }
    }

    @Override
    public void onShowHome() {
        if (!isFinishing()) {
            screenManager.showMainFeedFromSplash(SplashActivity.this);
        }
    }
}
