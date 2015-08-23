package com.elpatika.stepic.view;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.elpatika.stepic.R;
import com.elpatika.stepic.base.BaseFragmentActivity;


public class SplashActivity extends BaseFragmentActivity {

    // Splash screen wait time
    private static final int SPLASH_TIME_OUT = 3000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        overridePendingTransition(R.anim.slide_in_from_end, R.anim.slide_out_to_start);

        showLaunchScreen(SplashActivity.this, false);

    }

    public void showLaunchScreen(Context context, boolean overrideAnimation) {
        Intent launchIntent = new Intent(context, LaunchActivity.class);
        launchIntent.putExtra(LaunchActivity.OVERRIDE_ANIMATION_FLAG, overrideAnimation);
        if ( context instanceof Activity)
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        else
            launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(launchIntent);
    }
}
