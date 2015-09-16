package org.stepic.droid.core;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import org.stepic.droid.view.activities.LaunchActivity;
import org.stepic.droid.view.activities.LoginActivity;
import org.stepic.droid.view.activities.MainFeedActivity;
import org.stepic.droid.view.activities.RegisterActivity;

import javax.inject.Singleton;

import javax.inject.Inject;

@Singleton
public class ScreenManager implements IScreenManager {
    @Inject
    public ScreenManager () {

    }

    @Override
    public void showLaunchScreen(Context context, boolean overrideAnimation) {
        Intent launchIntent = new Intent(context, LaunchActivity.class);
        launchIntent.putExtra(LaunchActivity.OVERRIDE_ANIMATION_FLAG, overrideAnimation);
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(launchIntent);
    }


    @Override
    public void showRegistration(Activity sourceActivity) {
        Intent launchIntent = new Intent(sourceActivity, RegisterActivity.class);
        sourceActivity.startActivity(launchIntent);
    }

    @Override
    public void showLogin(Context sourceActivity) {
        Intent loginIntent = new Intent(sourceActivity, LoginActivity.class);
        sourceActivity.startActivity(loginIntent);
    }

    @Override
    public void showMainFeed(Context sourceActivity) {
        //todo finish all activities which exist for login (launch, splash, etc).
        Intent intent = new Intent(sourceActivity, MainFeedActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        /*
        Using CLEAR_TOP flag, causes the activity to be re-created every time.
        This reloads the list of courses. We don't want that.
        Using REORDER_TO_FRONT solves this problem
         */
//        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        sourceActivity.startActivity(intent);

        // let login screens be ended
//        Intent loginIntent = new Intent();
//        loginIntent.setAction(AppConstants.USER_LOG_IN);
//        sourceActivity.sendBroadcast(loginIntent);

    }


}
