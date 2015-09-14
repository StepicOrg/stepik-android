package com.elpatika.stepic.view.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.elpatika.stepic.R;
import com.elpatika.stepic.base.StepicBaseFragmentActivity;
import com.elpatika.stepic.concurrency.LoginTask;
import com.elpatika.stepic.util.SharedPreferenceHelper;
import com.elpatika.stepic.web.AuthenticationStepicResponse;

import butterknife.Bind;
import butterknife.ButterKnife;

public class LoginActivity extends StepicBaseFragmentActivity {

    @Bind(R.id.actionbar_close_btn)
    View mCloseButton;

    @Bind(R.id.login_button_layout)
    View mLoginBtn;

    @Bind(R.id.email_et)
    EditText mLoginText;

    @Bind(R.id.password_et)
    EditText mPasswordText;

    @Bind(R.id.login_spinner)
    ProgressBar mProgressLoogin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
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
//                onUserLoginSuccess(); // todo: FOR DEBUG ONLY
                tryLogin();
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.no_transition, R.anim.slide_out_to_bottom);
    }

    private void tryLogin() {
        String login = mLoginText.getText().toString().trim();
        String password = mPasswordText.getText().toString().trim();

        LoginTask loginTask = new LoginTask(this, login, password) {
            @Override
            protected void onSuccess(AuthenticationStepicResponse result) {
                super.onSuccess(result);
                SharedPreferenceHelper preferenceHelper = mShell.getSharedPreferenceHelper();
                preferenceHelper.storeAuthInfo(LoginActivity.this, result);
                try {
                    if (result != null) {
                        onUserLoginSuccess();
                    } else {
                        String errorMsg = "Error is occurred";
                        throw new Exception(errorMsg);
                    }
                } catch (Exception ex) {
                    //ignore
                }
            }

            @Override
            protected void onException(Exception exception) {
                super.onException(exception);
                onUserLoginFailure(exception);
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
        ex.printStackTrace();
        Toast.makeText(getApplicationContext(), "Fail exception: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
    }

}
