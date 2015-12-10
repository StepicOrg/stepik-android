package org.stepic.droid.view.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.yandex.metrica.YandexMetrica;

import org.stepic.droid.R;
import org.stepic.droid.base.FragmentActivityBase;
import org.stepic.droid.preferences.SharedPreferenceHelper;
import org.stepic.droid.social.SocialManager;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.DpPixelsHelper;
import org.stepic.droid.util.ProgressHelper;
import org.stepic.droid.view.adapters.SocialAuthAdapter;
import org.stepic.droid.view.decorators.SpacesItemDecorationHorizontal;
import org.stepic.droid.web.AuthenticationStepicResponse;
import org.stepic.droid.web.IApi;

import java.net.ProtocolException;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class LoginActivity extends FragmentActivityBase {

    @Bind(R.id.actionbar_close_btn_layout)
    View mCloseButton;

    @Bind(org.stepic.droid.R.id.login_button_layout)
    View mLoginBtn;

    @Bind(org.stepic.droid.R.id.email_et)
    EditText mLoginText;

    @Bind(org.stepic.droid.R.id.password_et)
    EditText mPasswordText;

    @Bind(R.id.forgot_password_tv)
    TextView mForgotPassword;

    @Bind(R.id.root_view)
    View mRootView;

    @Bind(R.id.social_list)
    RecyclerView mSocialRecyclerView;

    ProgressDialog mProgressLogin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(org.stepic.droid.R.layout.activity_login);
        ButterKnife.bind(this);
        overridePendingTransition(org.stepic.droid.R.anim.slide_in_from_bottom, org.stepic.droid.R.anim.no_transition);

        hideSoftKeypad();

        float pixelForPadding = DpPixelsHelper.convertDpToPixel(4f, this);//pixelForPadding * (count+1)
        float widthOfItem = getResources().getDimension(R.dimen.height_of_social);//width == height
        int count = SocialManager.SocialType.values().length;
        float widthOfAllItems = widthOfItem * count + pixelForPadding * (count + 1);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int widthOfScreen = size.x;


        mSocialRecyclerView.addItemDecoration(new SpacesItemDecorationHorizontal((int) pixelForPadding));//30 is ok
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        if (widthOfScreen > widthOfAllItems) {
            int padding = (int) (widthOfScreen - widthOfAllItems) / 2;
            mSocialRecyclerView.setPadding(padding, 0, 0, 0);
        }

        mSocialRecyclerView.setLayoutManager(layoutManager);
        mSocialRecyclerView.setAdapter(new SocialAuthAdapter(this));

        mProgressLogin = new ProgressDialog(this);
        mProgressLogin.setTitle(getString(R.string.loading));
        mProgressLogin.setMessage(getString(R.string.loading_message));
        mProgressLogin.setCancelable(false);


        mLoginText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    mPasswordText.requestFocus();
                    handled = true;
                }
                return handled;
            }
        });

        mPasswordText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
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


        mCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                YandexMetrica.reportEvent(AppConstants.METRICA_CLICK_SIGN_IN_ON_SIGN_IN_SCREEN);
                tryLogin();
            }
        });

        mForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mShell.getScreenProvider().openRemindPassword(LoginActivity.this);
            }
        });


        mRootView.requestFocus();

        //if we redirect from social:

        Intent intent = getIntent();
        if (intent.getData() != null) {
            try {
                String code = intent.getData().getQueryParameter("code");

                hideSoftKeypad();
                ProgressHelper.activate(mProgressLogin);
                mShell.getApi().authWithCode(code).enqueue(new Callback<AuthenticationStepicResponse>() {
                    @Override
                    public void onResponse(Response<AuthenticationStepicResponse> response, Retrofit retrofit) {
                        successLogin(response, retrofit);
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        failLogin(t);
                    }
                });
            } catch (Throwable t) {
                YandexMetrica.reportError("callback_from_social_login", t);
            }
        }

    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(org.stepic.droid.R.anim.no_transition, org.stepic.droid.R.anim.slide_out_to_bottom);
    }

    private void tryLogin() {
        hideSoftKeypad();

        String login = mLoginText.getText().toString().trim();
        String password = mPasswordText.getText().toString().trim();


        ProgressHelper.activate(mProgressLogin);
        IApi api = mShell.getApi();
        api.authWithLoginPassword(login, password).enqueue(new Callback<AuthenticationStepicResponse>() {
            @Override
            public void onResponse(Response<AuthenticationStepicResponse> response, Retrofit retrofit) {
                successLogin(response, retrofit);
            }

            @Override
            public void onFailure(Throwable t) {
                failLogin(t);
            }
        });
    }


    private void successLogin(Response<AuthenticationStepicResponse> response, Retrofit retrofit) {
        SharedPreferenceHelper preferenceHelper = mShell.getSharedPreferenceHelper();
        AuthenticationStepicResponse authStepic = response.body();
        preferenceHelper.storeAuthInfo(authStepic);

        ProgressHelper.dismiss(mProgressLogin);

        if (authStepic != null) {
            YandexMetrica.reportEvent(AppConstants.METRICA_SUCCESS_LOGIN);
            onUserLoginSuccess();
        } else {
            YandexMetrica.reportEvent(AppConstants.METRICA_FAIL_LOGIN);
            ProgressHelper.dismiss(mProgressLogin);
        }
    }

    private void failLogin(Throwable t) {
        YandexMetrica.reportEvent(AppConstants.METRICA_FAIL_LOGIN);
        YandexMetrica.reportError(AppConstants.METRICA_FAIL_LOGIN, t);
        ProgressHelper.dismiss(mProgressLogin);
        if (t != null) {
            if (t instanceof ProtocolException) {
                Toast.makeText(LoginActivity.this, R.string.failLogin, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(LoginActivity.this, R.string.failLoginConnectionProblems, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        mCloseButton.setOnClickListener(null);
        mLoginBtn.setOnClickListener(null);
        mForgotPassword.setOnClickListener(null);
        super.onDestroy();

    }

    private void onUserLoginSuccess() {
        mShell.getScreenProvider().showMainFeed(this);
        finish();
    }

}
