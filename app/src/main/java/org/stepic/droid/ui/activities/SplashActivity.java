package org.stepic.droid.ui.activities;


import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import org.stepic.droid.R;
import org.stepic.droid.base.App;
import org.stepic.droid.core.presenters.SplashPresenter;
import org.stepic.droid.core.presenters.contracts.SplashView;
import org.stepic.droid.util.AppConstants;

import java.util.Arrays;

import javax.inject.Inject;


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

        defineShortcuts();

        splashPresenter.attachView(this);
        splashPresenter.onSplashCreated();
    }

    private void defineShortcuts() {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.N_MR1) {
            return;
        }
        ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);
        if (shortcutManager == null) {
            return;
        }

        Intent catalogIntent = screenManager.getCatalogIntent(getApplicationContext());
        catalogIntent.setAction(AppConstants.OPEN_SHORTCUT_CATALOG);
        ShortcutInfo catalogShortcut = createShortcutInfo(AppConstants.CATALOG_SHORTCUT_ID, R.string.catalog_title, catalogIntent, R.drawable.ic_shortcut_find_courses);

        Intent profileIntent = screenManager.getMyProfileIntent(getApplicationContext());
        profileIntent.setAction(AppConstants.OPEN_SHORTCUT_PROFILE);
        ShortcutInfo profileShortcut = createShortcutInfo(AppConstants.PROFILE_SHORTCUT_ID, R.string.profile_title, profileIntent, R.drawable.ic_shortcut_profile);

        shortcutManager.setDynamicShortcuts(Arrays.asList(catalogShortcut, profileShortcut));
    }

    @NonNull
    @TargetApi(25)
    private ShortcutInfo createShortcutInfo(String id, @StringRes int titleRes, Intent catalogIntent, @DrawableRes int iconRes) {
        String title = getString(titleRes);
        return new ShortcutInfo.Builder(this, id)
                .setShortLabel(title)
                .setLongLabel(title)
                .setIcon(Icon.createWithResource(this, iconRes))
                .setIntent(catalogIntent)
                .build();
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
