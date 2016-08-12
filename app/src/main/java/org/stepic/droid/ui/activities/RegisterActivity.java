package org.stepic.droid.ui.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.squareup.okhttp.ResponseBody;

import org.jetbrains.annotations.Nullable;
import org.stepic.droid.R;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.base.FragmentActivityBase;
import org.stepic.droid.core.ActivityFinisher;
import org.stepic.droid.core.ProgressHandler;
import org.stepic.droid.util.ProgressHelper;
import org.stepic.droid.util.ValidatorUtil;
import org.stepic.droid.web.RegistrationResponse;

import java.lang.annotation.Annotation;

import butterknife.BindView;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnFocusChange;
import retrofit.Callback;
import retrofit.Converter;
import retrofit.Response;
import retrofit.Retrofit;


public class RegisterActivity extends FragmentActivityBase {

    public static final String ERROR_DELIMITER = " ";

    @BindView(R.id.root_view)
    View mRootView;

    @BindView(R.id.sign_up_btn)
    Button mCreateAccountButton;

    @BindView(R.id.actionbar_close_btn_layout)
    View mCloseButton;

    @BindView(R.id.first_name_reg)
    TextView mFirstNameView;

    @BindView(R.id.second_name_reg)
    TextView mSecondNameView;

    @BindView(R.id.email_reg)
    TextView mEmailView;

    @BindView(R.id.password_reg)
    TextView mPassword;

    @BindView(R.id.first_name_reg_wrapper)
    TextInputLayout mFirstNameViewWrapper;

    @BindView(R.id.second_name_reg_wrapper)
    TextInputLayout mSecondNameViewWrapper;

    @BindView(R.id.email_reg_wrapper)
    TextInputLayout mEmailViewWrapper;

    @BindView(R.id.password_reg_wrapper)
    TextInputLayout mPasswordWrapper;

    @BindString(R.string.password_too_short)
    String mPasswordTooShortMessage;


    ProgressDialog mProgress;
    TextWatcher mPasswordWatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(org.stepic.droid.R.layout.activity_register);
        unbinder = ButterKnife.bind(this);
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

        mPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    createAccount();
                    handled = true;
                }
                return handled;
            }
        });

        mPassword.addTextChangedListener(mPasswordWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (ValidatorUtil.isPasswordLengthValid(s.length())) {
                    hideError(mPasswordWrapper);
                }
            }
        });

        mRootView.requestFocus();
    }


    private void createAccount() {
        String firstName = mFirstNameView.getText().toString().trim();
        String lastName = mSecondNameView.getText().toString().trim();
        final String email = mEmailView.getText().toString().trim();
        final String password = mPassword.getText().toString();

        analytic.reportEvent(Analytic.Interaction.CLICK_REGISTER_BUTTON);

        boolean isOk = true;

        if (!ValidatorUtil.isPasswordValid(password)) {
            showError(mPasswordWrapper, mPasswordTooShortMessage);
            isOk = false;
        }

        if (isOk) {
            hideError(mFirstNameViewWrapper);
            hideError(mSecondNameViewWrapper);
            hideError(mEmailViewWrapper);
            hideError(mPasswordWrapper);

            mShell.getApi().signUp(firstName, lastName, email, password).enqueue(new Callback<RegistrationResponse>() {
                @Override
                public void onResponse(Response<RegistrationResponse> response, Retrofit retrofit) {
                    ProgressHelper.dismiss(mProgress);
                    if (response.isSuccess()) {
                        analytic.reportEvent(FirebaseAnalytics.Event.SIGN_UP);
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
                        Converter<ResponseBody, RegistrationResponse> errorConverter =
                                retrofit.responseConverter(RegistrationResponse.class, new Annotation[0]);
                        RegistrationResponse error = null;
                        try {
                            error = errorConverter.convert(response.errorBody());
                        } catch (Exception e) {
                            analytic.reportError(Analytic.Error.REGISTRATION_IMPORTANT_ERROR, e); //it is unknown response Expected BEGIN_OBJECT but was STRING at line 1 column 1 path
                        }
                        handleErrorRegistrationResponse(error);
                    }

                }

                @Override
                public void onFailure(Throwable t) {
                    ProgressHelper.dismiss(mProgress);
                    Toast.makeText(RegisterActivity.this, R.string.connectionProblems, Toast.LENGTH_SHORT).show();
                }
            });
        }
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
        mPassword.removeTextChangedListener(mPasswordWatcher);
        mPassword.setOnEditorActionListener(null);
        super.onDestroy();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(org.stepic.droid.R.anim.no_transition, org.stepic.droid.R.anim.slide_out_to_bottom);
    }

    private void hideError(TextInputLayout textInputLayout) {
        if (textInputLayout != null) {
            textInputLayout.setError("");
            textInputLayout.setErrorEnabled(false);
        }
    }

    private void showError(TextInputLayout textInputLayout, String errorText) {
        if (textInputLayout != null) {
            textInputLayout.setErrorEnabled(true);
            textInputLayout.setError(errorText);
        }
    }

    private void handleErrorRegistrationResponse(@Nullable RegistrationResponse registrationResponse) {
        if (registrationResponse == null) return;
        showError(mEmailViewWrapper, getErrorString(registrationResponse.getEmail()));
        showError(mFirstNameViewWrapper, getErrorString(registrationResponse.getFirst_name()));
        showError(mSecondNameViewWrapper, getErrorString(registrationResponse.getLast_name()));
        showError(mPasswordWrapper, getErrorString(registrationResponse.getPassword()));
    }

    @Nullable
    private String getErrorString(String[] values) {
        if (values == null || values.length == 0) return null;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            sb.append(values[i]);
            if (i != values.length - 1) {
                sb.append(ERROR_DELIMITER);
            }
        }
        return sb.toString();
    }

    @OnFocusChange({R.id.email_reg, R.id.first_name_reg, R.id.second_name_reg})
    public void setClearErrorOnFocus(View view, boolean hasFocus) {
        if (hasFocus) {
            if (view.getId() == R.id.email_reg) {
                hideError(mEmailViewWrapper);
            }

            if (view.getId() == R.id.first_name_reg) {
                hideError(mFirstNameViewWrapper);
            }
            if (view.getId() == R.id.second_name_reg) {
                hideError(mSecondNameViewWrapper);
            }
        }
    }

}
