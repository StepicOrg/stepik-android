package org.stepic.droid.ui.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
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

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.fabric.sdk.android.Fabric;

public class LoginActivity extends FragmentActivityBase {

    @BindView(R.id.actionbar_close_btn_layout)
    View mCloseButton;

    @BindView(R.id.login_button_layout)
    View mLoginBtn;

    @BindView(R.id.email_et)
    EditText mLoginText;

    @BindView(R.id.password_et)
    EditText mPasswordText;

    @BindView(R.id.forgot_password_tv)
    TextView mForgotPassword;

    @BindView(R.id.root_view)
    View mRootView;

    @BindView(R.id.social_list)
    RecyclerView mSocialRecyclerView;

    private ProgressDialog mProgressLogin;

    ProgressHandler progressHandler;

    GoogleApiClient mGoogleApiClient;
    private CallbackManager callbackManager;
    private TwitterAuthClient twitterAuthClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        unbinder = ButterKnife.bind(this);
        overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.no_transition);

        hideSoftKeypad();

        TwitterAuthConfig authConfig = new TwitterAuthConfig(mConfig.getTwitterKey(), mConfig.getTwitterSecret());
        Fabric.with(this, new Twitter(authConfig));

        String serverClientId = mConfig.getGoogleServerClientId();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(Scopes.EMAIL), new Scope(Scopes.PROFILE))
                .requestServerAuthCode(serverClientId)
                .build();

        // Build GoogleAPIClient with the Google Sign-In API and the above options.
        // ATTENTION: This "addApi(AppIndex.API)"was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
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
                ProgressHelper.activate(mProgressLogin);
            }

            @Override
            public void dismiss() {
                ProgressHelper.dismiss(mProgressLogin);
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


        mSocialRecyclerView.addItemDecoration(new SpacesItemDecorationHorizontal((int) pixelForPadding));//30 is ok
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        if (widthOfScreen > widthOfAllItems) {
            int padding = (int) (widthOfScreen - widthOfAllItems) / 2;
            mSocialRecyclerView.setPadding(padding, 0, 0, 0);
        }

        mSocialRecyclerView.setLayoutManager(layoutManager);
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
                Toast.makeText(LoginActivity.this, "onSuccess", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancel() {
                Toast.makeText(LoginActivity.this, "onCancel", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException exception) {
                Toast.makeText(LoginActivity.this, "onError", Toast.LENGTH_SHORT).show();
            }
        });
        twitterAuthClient = new TwitterAuthClient();
        Callback<TwitterSession> twitterSessionCallback = new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                Toast.makeText(LoginActivity.this, "twitter_success", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void failure(TwitterException exception) {
                Toast.makeText(LoginActivity.this, "twitter_failure", Toast.LENGTH_SHORT).show();
            }
        };
        mSocialRecyclerView.setAdapter(new SocialAuthAdapter(this, mGoogleApiClient, twitterAuthClient, twitterSessionCallback));

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
                analytic.reportEvent(Analytic.Interaction.CLICK_SIGN_IN_ON_SIGN_IN_SCREEN);
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

            mLoginManager.loginWithCode(code, progressHandler, new ActivityFinisher() {
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
        super.onStart();// ATTENTION: This was auto-generated to implement the App Indexing API.
// See https://g.co/AppIndexing/AndroidStudio for more information.
        mGoogleApiClient.connect();
        bus.register(this);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.start(mGoogleApiClient, getIndexApiAction());
    }

    @Override
    protected void onStop() {
        super.onStop();// ATTENTION: This was auto-generated to implement the App Indexing API.
// See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(mGoogleApiClient, getIndexApiAction());
        bus.unregister(this);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        mGoogleApiClient.disconnect();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.no_transition, R.anim.slide_out_to_bottom);
    }

    private void tryLogin() {
        String login = mLoginText.getText().toString();
        String password = mPasswordText.getText().toString();

        mLoginManager.login(login, password,
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
        mCloseButton.setOnClickListener(null);
        mLoginBtn.setOnClickListener(null);
        super.onDestroy();
    }

    @OnClick(R.id.forgot_password_tv)
    public void OnClickForgotPassword() {
        mShell.getScreenProvider().openRemindPassword(LoginActivity.this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        twitterAuthClient.onActivityResult(requestCode, resultCode, data);
        if (VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
// Пользователь успешно авторизовался
                Toast.makeText(LoginActivity.this, "vksuccess", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(VKError error) {
// Произошла ошибка авторизации (например, пользователь запретил авторизацию)
                Toast.makeText(LoginActivity.this, "vkerror", Toast.LENGTH_SHORT).show();
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

                mLoginManager.loginWithNativeProviderCode(authCode,
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
                                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
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

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Login Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }
}
