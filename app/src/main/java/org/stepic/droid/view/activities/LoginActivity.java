package org.stepic.droid.view.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.stepic.droid.base.StepicBaseFragmentActivity;
import org.stepic.droid.util.ProgressHelper;
import org.stepic.droid.util.SharedPreferenceHelper;
import org.stepic.droid.web.AuthenticationStepicResponse;
import org.stepic.droid.web.IApi;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class LoginActivity extends StepicBaseFragmentActivity {

    @Bind(org.stepic.droid.R.id.actionbar_close_btn)
    View mCloseButton;

    @Bind(org.stepic.droid.R.id.login_button_layout)
    View mLoginBtn;

    @Bind(org.stepic.droid.R.id.email_et)
    EditText mLoginText;

    @Bind(org.stepic.droid.R.id.password_et)
    EditText mPasswordText;

    @Bind(org.stepic.droid.R.id.login_spinner)
    ProgressBar mProgressLogin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(org.stepic.droid.R.layout.activity_login);
        ButterKnife.bind(this);
        overridePendingTransition(org.stepic.droid.R.anim.slide_in_from_bottom, org.stepic.droid.R.anim.no_transition);

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
        overridePendingTransition(org.stepic.droid.R.anim.no_transition, org.stepic.droid.R.anim.slide_out_to_bottom);
    }

    private void tryLogin() {
        String login = mLoginText.getText().toString().trim();
        String password = mPasswordText.getText().toString().trim();


        ProgressHelper.activate(mProgressLogin);
        IApi api = mShell.getApi();
        api.authWithLoginPassword(login, password).enqueue(new Callback<AuthenticationStepicResponse>() {
            @Override
            public void onResponse(Response<AuthenticationStepicResponse> response, Retrofit retrofit) {
                SharedPreferenceHelper preferenceHelper = mShell.getSharedPreferenceHelper();
                AuthenticationStepicResponse authStepic = response.body();
                preferenceHelper.storeAuthInfo(authStepic);

                ProgressHelper.dismiss(mProgressLogin);

                if (authStepic != null) {
                    onUserLoginSuccess();
                } else {
                    ProgressHelper.dismiss(mProgressLogin);
                    String errorMsg = "Error is occurred";
                    Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                ////// FIXME: 04.10.15 show right message to user
                ProgressHelper.dismiss(mProgressLogin);
                Toast.makeText(LoginActivity.this, "Something wrong", Toast.LENGTH_LONG).show();
            }
        });
    }


    private void onUserLoginSuccess() {
        mShell.getScreenProvider().showMainFeed(this);
        finish();
    }

    private void onUserLoginFailure(Throwable ex) {
        //todo: show Error message to user
        Log.i("Error key", "Error in user login");
        ex.printStackTrace();
        Toast.makeText(getApplicationContext(), "Fail exception: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
    }

}
