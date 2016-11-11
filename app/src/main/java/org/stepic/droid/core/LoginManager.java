package org.stepic.droid.core;

import android.content.Context;
import android.widget.Toast;

import org.stepic.droid.R;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.preferences.SharedPreferenceHelper;
import org.stepic.droid.social.SocialManager;
import org.stepic.droid.ui.util.FailLoginSupplementaryHandler;
import org.stepic.droid.util.JsonHelper;
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
    private final IShell shell;
    private final Context context;
    private Analytic analytic;

    @Inject
    public LoginManager(IShell shell, Context appContext, Analytic analytic) {
        this.shell = shell;
        context = appContext;
        this.analytic = analytic;
    }


    @Override
    public void login(String rawLogin, String rawPassword, final ProgressHandler progressHandler, final ActivityFinisher finisher) {
        progressHandler.activate();

        String login = rawLogin.trim();

        IApi api = shell.getApi();
        api.authWithLoginPassword(login, rawPassword).enqueue(new Callback<AuthenticationStepicResponse>() {
            @Override
            public void onResponse(Response<AuthenticationStepicResponse> response, Retrofit retrofit) {
                progressHandler.dismiss();
                if (response.isSuccess()) {
                    successLogin(response, finisher, null);
                } else {
                    if (response.code() == 429) {
                        failLogin(new TooManyAttempts(JsonHelper.toJson(response.errorBody())), null);
                    } else {
                        failLogin(new ProtocolException(JsonHelper.toJson(response.errorBody())), null);
                    }
                }
            }

            @Override
            public void onFailure(Throwable t) {
                progressHandler.dismiss();
                failLogin(t, null);
            }
        });
    }

    @Override
    public void loginWithCode(String rawCode, final ProgressHandler progressHandler, final ActivityFinisher finisher) {
        String code = rawCode.trim();
        progressHandler.activate();
        shell.getApi().authWithCode(code).enqueue(new Callback<AuthenticationStepicResponse>() {
            @Override
            public void onResponse(Response<AuthenticationStepicResponse> response, Retrofit retrofit) {
                handleSuccess(progressHandler, response, finisher, null);
            }

            @Override
            public void onFailure(Throwable t) {
                progressHandler.dismiss();
                failLogin(t, null);
            }
        });
    }

    private void handleSuccess(ProgressHandler progressHandler, Response<AuthenticationStepicResponse> response, ActivityFinisher finisher, FailLoginSupplementaryHandler failLoginSupplementaryHandler) {
        progressHandler.dismiss();
        if (response.isSuccess()) {
            successLogin(response, finisher, failLoginSupplementaryHandler);
        } else {
            if (response.code() == 401) {
                failLogin(new LoginAlreadyUsedException("already used login"), failLoginSupplementaryHandler);
            } else {
                failLogin(new ProtocolException(JsonHelper.toJson(response.errorBody())), failLoginSupplementaryHandler);
            }
        }
    }

    @Override
    public void loginWithNativeProviderCode(String nativeCode, SocialManager.SocialType type, final ProgressHandler progressHandler, final ActivityFinisher finisher, final FailLoginSupplementaryHandler failLoginSupplementaryHandler) {
        boolean isAccessToken = false;
        if (type == SocialManager.SocialType.facebook) {
            isAccessToken = true;
        }
        String code = nativeCode.trim();
        progressHandler.activate();
        shell.getApi().authWithNativeCode(code, type, isAccessToken).enqueue(new Callback<AuthenticationStepicResponse>() {
            @Override
            public void onResponse(Response<AuthenticationStepicResponse> response, Retrofit retrofit) {
                handleSuccess(progressHandler, response, finisher, failLoginSupplementaryHandler);
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
            } else if (t instanceof TooManyAttempts) {
                errorTextResId = R.string.too_many_attempts;
            } else {
                errorTextResId = R.string.connectionProblems;
            }
            Toast.makeText(context, errorTextResId, Toast.LENGTH_LONG).show();
            if (failLoginSupplementaryHandler != null) {
                failLoginSupplementaryHandler.onFailLogin(t);
            }
        }
    }

    private void successLogin(Response<AuthenticationStepicResponse> response, ActivityFinisher finisher, FailLoginSupplementaryHandler failLoginSupplementaryHandler) {
        SharedPreferenceHelper preferenceHelper = shell.getSharedPreferenceHelper();
        AuthenticationStepicResponse authStepic = response.body();
        preferenceHelper.storeAuthInfo(authStepic);

        if (authStepic != null) {
            analytic.reportEvent(Analytic.Interaction.SUCCESS_LOGIN);
            shell.getScreenProvider().showMainFeed(context);
            finisher.onFinish();
        } else {
            failLogin(new ProtocolException(JsonHelper.toJson(response.errorBody())), failLoginSupplementaryHandler);
        }
    }

    private static class LoginAlreadyUsedException extends RuntimeException {
        LoginAlreadyUsedException(String message) {
            super(message);
        }
    }

    public static class TooManyAttempts extends RuntimeException {
        TooManyAttempts(String messsage) {
            super(messsage);
        }
    }

}
