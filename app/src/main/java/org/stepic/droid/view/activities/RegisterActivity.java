package org.stepic.droid.view.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.stepic.droid.R;
import org.stepic.droid.base.FragmentActivityBase;
import org.stepic.droid.core.ActivityFinisher;
import org.stepic.droid.core.ProgressHandler;
import org.stepic.droid.util.ProgressHelper;
import org.stepic.droid.web.RegistrationResponse;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;


public class RegisterActivity extends FragmentActivityBase {

    @Bind(R.id.root_view)
    View mRootView;

    @Bind(R.id.sign_up_btn)
    Button mCreateAccountButton;

    @Bind(R.id.actionbar_close_btn_layout)
    View mCloseButton;

    @Bind(R.id.first_name_reg)
    TextView mFirstNameView;

    @Bind(R.id.second_name_reg)
    TextView mSecondNameView;

    @Bind(R.id.email_reg)
    TextView mEmailView;

    @Bind(R.id.password_reg)
    TextView mPassword;

    ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(org.stepic.droid.R.layout.activity_register);
        ButterKnife.bind(this);
        overridePendingTransition(org.stepic.droid.R.anim.slide_in_from_bottom, org.stepic.droid.R.anim.no_transition);

        hideSoftKeypad();

        mCreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAccount();
            }
        });
        mCloseButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mProgress = new ProgressDialog(this);
        mProgress.setTitle(getString(R.string.loading));
        mProgress.setMessage(getString(R.string.loading_message));
        mProgress.setCancelable(false);

        mRootView.requestFocus();
    }


    private void createAccount() {
        String firstName = mFirstNameView.getText().toString().trim();
        String lastName = mSecondNameView.getText().toString().trim();
        final String email = mEmailView.getText().toString().trim();
        final String password = mPassword.getText().toString().trim();

        ProgressHelper.activate(mProgress);

        mShell.getApi().signUp(firstName, lastName, email, password).enqueue(new Callback<RegistrationResponse>() {
            @Override
            public void onResponse(Response<RegistrationResponse> response, Retrofit retrofit) {
                ProgressHelper.dismiss(mProgress);
                if (response.isSuccess()) {
                    mLoginManager.login(email, password, new ProgressHandler() {
                        @Override
                        public void activate() {
                            hideSoftKeypad();
                            ProgressHelper.activate(mProgress);
                        }

                        @Override
                        public void dismiss() {
                            ProgressHelper.dismiss(mProgress);
                        }
                    }, new ActivityFinisher() {
                        @Override
                        public void onFinish() {
                            finish();
                        }
                    });
                } else {
                    Toast.makeText(RegisterActivity.this, "Failure " + response.code(), Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(Throwable t) {
                ProgressHelper.dismiss(mProgress);
                Toast.makeText(RegisterActivity.this, "exception", Toast.LENGTH_SHORT).show();
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
    public void finish() {
        super.finish();
        overridePendingTransition(org.stepic.droid.R.anim.no_transition, org.stepic.droid.R.anim.slide_out_to_bottom);
    }


}
