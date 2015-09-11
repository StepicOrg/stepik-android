package com.elpatika.stepic.view.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;

import com.elpatika.stepic.R;
import com.elpatika.stepic.base.StepicBaseFragmentActivity;
import com.elpatika.stepic.util.AppConstants;
import com.elpatika.stepic.view.custom.SButton;
import com.elpatika.stepic.view.custom.STextView;

import roboguice.inject.InjectView;


public class LaunchActivity extends StepicBaseFragmentActivity {

    public static final String OVERRIDE_ANIMATION_FLAG = "override_animation_flag";

    @InjectView(R.id.sign_up_btn)
    private SButton mSignUpButton;

    @InjectView(R.id.sign_in_tv)
    private STextView mSignInTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        enableLoginCallback();
        setContentView(R.layout.activity_launch);

        //Activity override animation has to be handled if the Launch Activity
        //is called after user logs out and closes the Sign-in screen.
        if (getIntent().getBooleanExtra(OVERRIDE_ANIMATION_FLAG, false)) {
            overridePendingTransition(R.anim.no_transition, R.anim.slide_out_to_bottom);
        }

        mSignUpButton.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mShell.getScreenProvider().showRegistration(LaunchActivity.this);
            }
        }));

        mSignInTextView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mShell.getScreenProvider().showLogin(LaunchActivity.this);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disableLoginCallback();
    }

    //Broadcast Receiver to notify all activities to finish if user logs out
    private BroadcastReceiver loginReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };

    protected void enableLoginCallback() {
        // register for login listener
        IntentFilter filter = new IntentFilter();
        filter.addAction(AppConstants.USER_LOG_IN);
        registerReceiver(loginReceiver, filter);
    }

    protected void disableLoginCallback() {
        // un-register loginReceiver
        unregisterReceiver(loginReceiver);
    }
}
