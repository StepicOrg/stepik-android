package org.stepic.droid.ui.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;

import org.stepic.droid.R;
import org.stepic.droid.util.AppConstants;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LaunchActivity extends BackToExitActivityBase {

    public static final String OVERRIDE_ANIMATION_FLAG = "override_animation_flag";

    @BindView(R.id.sign_up_btn_activity_launch)
    View mSignUpButton;

    @BindView(R.id.sign_in_tv)
    View mSignInTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        unbinder = ButterKnife.bind(this);

        enableLoginCallback();

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
    protected void onStart() {
        super.onStart();
        bus.register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        bus.unregister(this);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        disableLoginCallback();
    }

    //Broadcast Receiver to notify all activities to onFinish if user logs out
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
