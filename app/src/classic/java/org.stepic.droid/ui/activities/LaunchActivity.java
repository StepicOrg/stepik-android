package org.stepic.droid.ui.activities;

import android.os.Bundle;
import android.view.View;

import org.stepic.droid.R;
import org.stepic.droid.analytic.Analytic;

import butterknife.BindView;
import butterknife.ButterKnife;


public class LaunchActivity extends BackToExitActivityBase {

    public static final String OVERRIDE_ANIMATION_FLAG = "override_animation_flag";

    @BindView(R.id.sign_up_btn_activity_launch)
    View signUpButton;

    @BindView(R.id.sign_in_tv)
    View signInTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        unbinder = ButterKnife.bind(this);

        //Activity override animation has to be handled if the Launch Activity
        //is called after user logs out and closes the Sign-in screen.
        if (getIntent().getBooleanExtra(OVERRIDE_ANIMATION_FLAG, false)) {
            overridePendingTransition(R.anim.no_transition, R.anim.slide_out_to_bottom);
        }

        signUpButton.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                analytic.reportEvent(Analytic.Interaction.CLICK_SIGN_UP);
                shell.getScreenProvider().showRegistration(LaunchActivity.this);
            }
        }));

        signInTextView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                analytic.reportEvent(Analytic.Interaction.CLICK_SIGN_IN);
                shell.getScreenProvider().showLogin(LaunchActivity.this);
            }
        });
    }
}
