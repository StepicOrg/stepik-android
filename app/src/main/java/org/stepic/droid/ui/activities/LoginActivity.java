package org.stepic.droid.ui.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import org.stepic.droid.R;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.base.FragmentActivityBase;
import org.stepic.droid.core.ActivityFinisher;
import org.stepic.droid.core.ProgressHandler;
import org.stepic.droid.ui.dialogs.LoadingProgressDialog;
import org.stepic.droid.util.ProgressHelper;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

@SuppressLint("GoogleAppIndexingApiWarning")
public class LoginActivity extends FragmentActivityBase {

    @BindView(R.id.actionbar_close_btn_layout)
    View closeButton;

    @BindView(R.id.login_button_layout)
    View loginButton;

    @BindView(R.id.email_et)
    EditText loginText;

    @BindView(R.id.password_et)
    EditText passwordText;

    @BindView(R.id.forgot_password_tv)
    TextView forgotPassword;

    @BindView(R.id.root_view)
    View rootView;

    @BindView(R.id.terms_privacy_login)
    TextView termsPrivacyTextView;

    @BindString(R.string.terms_message_login)
    String termsMessageHtml;

    private ProgressDialog progressLogin;

    ProgressHandler progressHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(null);
        setContentView(R.layout.activity_login);
        unbinder = ButterKnife.bind(this);
        overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.no_transition);

        hideSoftKeypad();
        termsPrivacyTextView.setMovementMethod(LinkMovementMethod.getInstance());
        termsPrivacyTextView.setText(textResolver.fromHtml(termsMessageHtml));


        progressHandler = new ProgressHandler() {
            @Override
            public void activate() {
                hideSoftKeypad();
                ProgressHelper.activate(progressLogin);
            }

            @Override
            public void dismiss() {
                ProgressHelper.dismiss(progressLogin);
            }
        };

        progressLogin = new LoadingProgressDialog(this);

        loginText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    passwordText.requestFocus();
                    handled = true;
                }
                return handled;
            }
        });

        passwordText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    tryLogin();
                    handled = true;
                }
                return handled;
            }
        });


        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                analytic.reportEvent(Analytic.Interaction.CLICK_SIGN_IN_ON_SIGN_IN_SCREEN);
                tryLogin();
            }
        });

        rootView.requestFocus();

        //if we redirect from social:

        Intent intent = getIntent();
        if (intent.getData() != null) {
            redirectFromSocial(intent);
        }

    }

    private void redirectFromSocial(Intent intent) {
        try {
            String code = intent.getData().getQueryParameter("code");

            loginManager.loginWithCode(code, progressHandler, new ActivityFinisher() {
                @Override
                public void onFinish() {
                    finish();
                }
            });
        } catch (Throwable t) {
            analytic.reportError(Analytic.Error.CALLBACK_SOCIAL, t);
        }
    }


    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.no_transition, R.anim.slide_out_to_bottom);
    }

    private void tryLogin() {
        String login = loginText.getText().toString();
        String password = passwordText.getText().toString();

        loginManager.login(login, password,
                progressHandler,
                new ActivityFinisher() {
                    @Override
                    public void onFinish() {
                        finish();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        closeButton.setOnClickListener(null);
        loginButton.setOnClickListener(null);
        super.onDestroy();
    }

    @OnClick(R.id.forgot_password_tv)
    public void OnClickForgotPassword() {
        shell.getScreenProvider().openRemindPassword(LoginActivity.this);
    }

}
