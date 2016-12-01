package org.stepic.droid.ui.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.method.LinkMovementMethod;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;

import org.stepic.droid.R;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.base.FragmentActivityBase;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.core.ActivityFinisher;
import org.stepic.droid.core.ProgressHandler;
import org.stepic.droid.social.SocialManager;
import org.stepic.droid.ui.adapters.SocialAuthAdapter;
import org.stepic.droid.ui.decorators.SpacesItemDecorationHorizontal;
import org.stepic.droid.ui.dialogs.LoadingProgressDialog;
import org.stepic.droid.ui.util.FailLoginSupplementaryHandler;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.DpPixelsHelper;
import org.stepic.droid.util.ProgressHelper;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends FragmentActivityBase {

    @BindView(R.id.actionbar_close_btn_layout)
    View closeButton;

    @BindView(R.id.login_button_layout)
    View loginBtn;

    @BindView(R.id.email_et)
    EditText loginText;

    @BindView(R.id.password_et)
    EditText passwordText;

    @BindView(R.id.forgot_password_tv)
    TextView forgotPassword;

    @BindView(R.id.root_view)
    View rootView;

    @BindView(R.id.social_list)
    RecyclerView socialRecyclerView;

    @BindView(R.id.terms_privacy_login)
    TextView termsPrivacyTextView;

    @BindString(R.string.terms_message_login)
    String termsMessageHtml;


    private ProgressDialog progressLogin;

    ProgressHandler progressHandler;

    GoogleApiClient googleApiClient;
    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        unbinder = ButterKnife.bind(this);
        overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.no_transition);

        hideSoftKeypad();
        termsPrivacyTextView.setMovementMethod(LinkMovementMethod.getInstance());
        termsPrivacyTextView.setText(textResolver.fromHtml(termsMessageHtml));

        String serverClientId = config.getGoogleServerClientId();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(Scopes.EMAIL), new Scope(Scopes.PROFILE))
                .requestServerAuthCode(serverClientId)
                .build();

        // Build GoogleAPIClient with the Google Sign-In API and the above options.
        // ATTENTION: This "addApi(AppIndex.API)"was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(MainApplication.getAppContext(), R.string.connectionProblems, Toast.LENGTH_SHORT).show();
                    }
                } /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addApi(AppIndex.API).build();

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

        float pixelForPadding = DpPixelsHelper.convertDpToPixel(4f, this);//pixelForPadding * (count+1)
        float widthOfItem = getResources().getDimension(R.dimen.height_of_social);//width == height
        int count = SocialManager.SocialType.values().length;
        float widthOfAllItems = widthOfItem * count + pixelForPadding * (count + 1);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int widthOfScreen = size.x;


        socialRecyclerView.addItemDecoration(new SpacesItemDecorationHorizontal((int) pixelForPadding));//30 is ok
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        if (widthOfScreen > widthOfAllItems) {
            int padding = (int) (widthOfScreen - widthOfAllItems) / 2;
            socialRecyclerView.setPadding(padding, 0, 0, 0);
        }

        socialRecyclerView.setLayoutManager(layoutManager);
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult) {
                loginManager.loginWithNativeProviderCode(loginResult.getAccessToken().getToken(),
                        SocialManager.SocialType.facebook,
                        progressHandler,
                        new ActivityFinisher() {
                            @Override
                            public void onFinish() {
                                finish();
                            }
                        },
                        new FailLoginSupplementaryHandler() {
                            @Override
                            public void onFailLogin(Throwable t) {
                                LoginManager.getInstance().logOut();
                            }
                        });
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException exception) {
                onInternetProblems();
            }
        });
        socialRecyclerView.setAdapter(new SocialAuthAdapter(this, googleApiClient));

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
        loginBtn.setOnClickListener(new View.OnClickListener() {
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
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
        bus.register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        bus.unregister(this);
        googleApiClient.disconnect();
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
        loginBtn.setOnClickListener(null);
        super.onDestroy();
    }

    @OnClick(R.id.forgot_password_tv)
    public void OnClickForgotPassword() {
        shell.getScreenProvider().openRemindPassword(LoginActivity.this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                loginManager.loginWithNativeProviderCode(res.accessToken,
                        SocialManager.SocialType.vk,
                        progressHandler,
                        new ActivityFinisher() {
                            @Override
                            public void onFinish() {
                                finish();
                            }
                        },
                        new FailLoginSupplementaryHandler() {
                            @Override
                            public void onFailLogin(Throwable t) {
                                VKSdk.logout();
                            }
                        });
            }

            @Override
            public void onError(VKError error) {
                if (error.errorCode == VKError.VK_REQUEST_HTTP_FAILED) {
                    onInternetProblems();
                }
            }
        })) {
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AppConstants.REQUEST_CODE_GOOGLE_SIGN_IN && resultCode == Activity.RESULT_OK) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                if (account == null) {
                    onInternetProblems();
                    return;
                }
                String authCode = account.getServerAuthCode();

                loginManager.loginWithNativeProviderCode(authCode,
                        SocialManager.SocialType.google,
                        progressHandler,
                        new ActivityFinisher() {
                            @Override
                            public void onFinish() {
                                finish();
                            }
                        },
                        new FailLoginSupplementaryHandler() {
                            @Override
                            public void onFailLogin(Throwable t) {
                                Auth.GoogleSignInApi.signOut(googleApiClient);
                            }
                        });
            } else {
                onInternetProblems();
            }
        }
    }

    private void onInternetProblems() {
        Toast.makeText(this, R.string.connectionProblems, Toast.LENGTH_SHORT).show();
    }
}
