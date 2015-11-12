package org.stepic.droid.view.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;

import com.yandex.metrica.YandexMetrica;

import org.stepic.droid.R;
import org.stepic.droid.base.FragmentActivityBase;
import org.stepic.droid.util.AppConstants;

import butterknife.Bind;
import butterknife.ButterKnife;


public class LaunchActivity extends FragmentActivityBase {

    public static final String OVERRIDE_ANIMATION_FLAG = "override_animation_flag";

    @Bind(R.id.sign_up_btn)
    View mSignUpButton;

    @Bind(R.id.sign_in_tv)
    View mSignInTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        ButterKnife.bind(this);

        enableLoginCallback();

        //Activity override animation has to be handled if the Launch Activity
        //is called after user logs out and closes the Sign-in screen.
        if (getIntent().getBooleanExtra(OVERRIDE_ANIMATION_FLAG, false)) {
            overridePendingTransition(R.anim.no_transition, R.anim.slide_out_to_bottom);
        }

        mSignUpButton.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                YandexMetrica.reportEvent(AppConstants.METRICA_CLICK_SIGN_UP);
                mShell.getScreenProvider().openSignUpInWeb(LaunchActivity.this);
            }
        }));

        mSignInTextView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                YandexMetrica.reportEvent(AppConstants.METRICA_CLICK_SIGN_IN);
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
