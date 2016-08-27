package org.stepic.droid.core;

import android.content.Context;
import android.widget.Toast;

import org.stepic.droid.R;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.preferences.SharedPreferenceHelper;
import org.stepic.droid.social.SocialManager;
import org.stepic.droid.util.JsonHelper;
import org.stepic.droid.ui.util.FailLoginSupplementaryHandler;
import org.stepic.droid.web.AuthenticationStepicResponse;
import org.stepic.droid.web.IApi;

import java.net.ProtocolException;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

@Singleton
public class LoginManager implements ILoginManager {
    private final IShell mShell;
    private final Context mContext;
    private Analytic analytic;

    @Inject
    public LoginManager(IShell shell, Context appContext, Analytic analytic) {
        mShell = shell;
        mContext = appContext;
        this.analytic = analytic;
    }


    @Override
    public void login(String rawLogin, String rawPassword, final ProgressHandler progressHandler, final ActivityFinisher finisher) {
        progressHandler.activate();

        String login = rawLogin.trim();

        IApi api = mShell.getApi();
        api.authWithLoginPassword(login, rawPassword).enqueue(new Callback<AuthenticationStepicResponse>() {
            @Override
            public void onResponse(Response<AuthenticationStepicResponse> response, Retrofit retrofit) {
                progressHandler.dismiss();
                if (response.isSuccess()) {
                    successLogin(response, finisher);
                } else {
                    failLogin(new ProtocolException(JsonHelper.toJson(response.errorBody())));
                }
            }

            @Override
            public void onFailure(Throwable t) {
                progressHandler.dismiss();
                failLogin(t);
            }
        });
    }

    @Override
    public void loginWithCode(String rawCode, final ProgressHandler progressHandler, final ActivityFinisher finisher) {
        String code = rawCode.trim();
        progressHandler.activate();
        mShell.getApi().authWithCode(code).enqueue(new Callback<AuthenticationStepicResponse>() {
            @Override
            public void onResponse(Response<AuthenticationStepicResponse> response, Retrofit retrofit) {
                handleSuccess(progressHandler, response, finisher);
            }

            @Override
            public void onFailure(Throwable t) {
                progressHandler.dismiss();
                failLogin(t);
            }
        });
    }

    private void handleSuccess(ProgressHandler progressHandler, Response<AuthenticationStepicResponse> response, ActivityFinisher finisher) {
        progressHandler.dismiss();
        if (response.isSuccess()) {
            successLogin(response, finisher);
        } else {
            if (response.code() == 401) {
                failLogin(new LoginAlreadyUsedException("already used login"));
            } else {
                failLogin(new ProtocolException(JsonHelper.toJson(response.errorBody())));
            }
        }
    }

    @Override
    public void loginWithNativeProviderCode(String nativeCode, SocialManager.SocialType type, final ProgressHandler progressHandler, final ActivityFinisher finisher, final FailLoginSupplementaryHandler failLoginSupplementaryHandler) {
        String code = nativeCode.trim();
        progressHandler.activate();
        mShell.getApi().authWithNativeCode(code, type).enqueue(new Callback<AuthenticationStepicResponse>() {
            @Override
            public void onResponse(Response<AuthenticationStepicResponse> response, Retrofit retrofit) {
                handleSuccess(progressHandler, response, finisher);
            }

            @Override
            public void onFailure(Throwable t) {
                progressHandler.dismiss();
                failLogin(t, failLoginSupplementaryHandler);
            }
        });
    }

    private void failLogin(Throwable t, final FailLoginSupplementaryHandler failLoginSupplementaryHandler) {
        analytic.reportEvent(Analytic.Error.FAIL_LOGIN);
        analytic.reportError(Analytic.Error.FAIL_LOGIN, t);
        if (t != null) {
            int errorTextResId;
            if (t instanceof ProtocolException) {
                errorTextResId = R.string.failLogin;
            } else if (t instanceof LoginAlreadyUsedException) {
                errorTextResId = R.string.email_already_used;
            } else {
                errorTextResId = R.string.connectionProblems;
            }
            Toast.makeText(mContext, errorTextResId, Toast.LENGTH_LONG).show();
            if (failLoginSupplementaryHandler != null) {
                failLoginSupplementaryHandler.onFailLogin(t);
            }
        }
    }

    private void failLogin(Throwable t) {
        failLogin(t, null);
    }

    private void successLogin(Response<AuthenticationStepicResponse> response, ActivityFinisher finisher) {
        SharedPreferenceHelper preferenceHelper = mShell.getSharedPreferenceHelper();
        AuthenticationStepicResponse authStepic = response.body();
        preferenceHelper.storeAuthInfo(authStepic);

        if (authStepic != null) {
            analytic.reportEvent(Analytic.Interaction.SUCCESS_LOGIN);
            mShell.getScreenProvider().showMainFeed(mContext);
            finisher.onFinish();
        } else {
            failLogin(new ProtocolException(JsonHelper.toJson(response.errorBody())));
        }
    }

    public static class LoginAlreadyUsedException extends RuntimeException {
        LoginAlreadyUsedException(String message) {
            super(message);
        }
    }

}
