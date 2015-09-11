package com.elpatika.stepic.view.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.elpatika.stepic.R;
import com.elpatika.stepic.base.StepicBaseFragmentActivity;
import com.elpatika.stepic.concurrency.LoginTask;
import com.elpatika.stepic.util.SharedPreferenceHelper;
import com.elpatika.stepic.web.AuthenticationStepicResponse;

import roboguice.inject.InjectView;

public class LoginActivity extends StepicBaseFragmentActivity {

    @InjectView (R.id.actionbar_close_btn)
    View mCloseButton;

    @InjectView (R.id.login_button_layout)
    View mLoginBtn;

    @InjectView (R.id.email_et)
    EditText mLoginText;

    @InjectView (R.id.password_et)
    EditText mPasswordText;

    @InjectView (R.id.login_spinner)
    ProgressBar mProgressLoogin;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.no_transition);

        hideSoftKeypad();

        mCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tryLogin();
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.no_transition, R.anim.slide_out_to_bottom);
    }

    private void tryLogin () {
        String login = mLoginText.getText().toString().trim();
        String password = mPasswordText.getText().toString().trim();

        LoginTask loginTask = new LoginTask(this, login, password) {
            @Override
            public void onSuccess(AuthenticationStepicResponse result) {
                SharedPreferenceHelper preferenceHelper = mShell.getSharedPreferenceHelper();
                preferenceHelper.storeAuthInfo(LoginActivity.this, result);
                try {
                    if (result != null) {
                        onUserLoginSuccess();
                    } else {
                        String errorMsg = "Error is occurred";
                        throw new Exception(errorMsg);
                    }
                } catch(Exception ex) {
                    handle(ex);
                }
            }

            @Override
            public void onException(Exception ex) {
                onUserLoginFailure(ex);
            }
        };
        loginTask.setProgressBar(mProgressLoogin);
        loginTask.execute();
    }


    private void onUserLoginSuccess() {
        mShell.getScreenProvider().showMainFeed(this);
        finish();
    }

    private void onUserLoginFailure(Exception ex) {
        //todo: show Error message to user
        Log.i("Error key", "Error in user login");
    }

}
