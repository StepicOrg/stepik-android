package com.elpatika.stepic.core;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.elpatika.stepic.view.LaunchActivity;
import com.elpatika.stepic.view.LoginActivity;
import com.elpatika.stepic.view.MainFeedActivity;
import com.elpatika.stepic.view.RegisterActivity;


public class ScreenManager implements IScreenManager {

    @Override
    public void showLaunchScreen(Context context, boolean overrideAnimation) {
        Intent launchIntent = new Intent(context, LaunchActivity.class);
        launchIntent.putExtra(LaunchActivity.OVERRIDE_ANIMATION_FLAG, overrideAnimation);
        if (context instanceof Activity)
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        else
            launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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
        if ( !(sourceActivity instanceof  Activity) )
            loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        sourceActivity.startActivity(loginIntent);
    }

    @Override
    public void showMainFeed(Context sourceActivity) {
        Intent intent = new Intent(sourceActivity, MainFeedActivity.class);
        /*
        Using CLEAR_TOP flag, causes the activity to be re-created every time.
        This reloads the list of courses. We don't want that.
        Using REORDER_TO_FRONT solves this problem
         */
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        sourceActivity.startActivity(intent);

        // let login screens be ended
//        Intent loginIntent = new Intent();
//        loginIntent.setAction(AppConstants.USER_LOG_IN);
//        sourceActivity.sendBroadcast(loginIntent);

    }


}
