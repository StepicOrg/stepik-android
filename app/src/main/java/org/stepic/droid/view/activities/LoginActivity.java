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

import com.yandex.metrica.YandexMetrica;

import org.stepic.droid.R;
import org.stepic.droid.base.FragmentActivityBase;
import org.stepic.droid.core.ActivityFinisher;
import org.stepic.droid.core.ProgressHandler;
import org.stepic.droid.social.SocialManager;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.DpPixelsHelper;
import org.stepic.droid.util.ProgressHelper;
import org.stepic.droid.view.adapters.SocialAuthAdapter;
import org.stepic.droid.view.custom.LoadingProgressDialog;
import org.stepic.droid.view.decorators.SpacesItemDecorationHorizontal;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

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

    private ProgressDialog mProgressLogin;


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

        mProgressLogin = new LoadingProgressDialog(this);

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

        mRootView.requestFocus();

        //if we redirect from social:

        Intent intent = getIntent();
        if (intent.getData() != null) {
            redirectFromSocial(intent);
        }

    }

    private void redirectFromSocial(Intent intent) {
        try {
            String code = intent.getData().getQueryParameter("code");
            mLoginManager.loginWithCode(code, new ProgressHandler() {
                @Override
                public void activate() {
                    hideSoftKeypad();
                    ProgressHelper.activate(mProgressLogin);
                }

                @Override
                public void dismiss() {
                    ProgressHelper.dismiss(mProgressLogin);
                }
            }, new ActivityFinisher() {
                @Override
                public void onFinish() {
                   finish();
                }
            });
        } catch (Throwable t) {
            YandexMetrica.reportError("callback_from_social_login", t);
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
    public void finish() {
        super.finish();
        overridePendingTransition(org.stepic.droid.R.anim.no_transition, org.stepic.droid.R.anim.slide_out_to_bottom);
    }

    private void tryLogin() {
        String login = mLoginText.getText().toString();
        String password = mPasswordText.getText().toString();

        mLoginManager.login(login, password,
                new ProgressHandler() {
                    @Override
                    public void activate() {
                        hideSoftKeypad();
                        ProgressHelper.activate(mProgressLogin);
                    }

                    @Override
                    public void dismiss() {
                        ProgressHelper.dismiss(mProgressLogin);
                    }
                },
                new ActivityFinisher() {
                    @Override
                    public void onFinish() {
                        finish();
                    }
                });
    }



    @Override
    protected void onDestroy() {
        mCloseButton.setOnClickListener(null);
        mLoginBtn.setOnClickListener(null);
        super.onDestroy();
    }

    @OnClick(R.id.forgot_password_tv)
    public void OnClickForgotPassword() {
        mShell.getScreenProvider().openRemindPassword(LoginActivity.this);
    }

}
