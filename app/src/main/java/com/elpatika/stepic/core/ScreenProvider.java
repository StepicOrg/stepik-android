package com.elpatika.stepic.core;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.elpatika.stepic.view.LaunchActivity;
import com.elpatika.stepic.view.LoginActivity;
import com.elpatika.stepic.view.RegisterActivity;


public class ScreenProvider implements IScreenProvider {

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


}
