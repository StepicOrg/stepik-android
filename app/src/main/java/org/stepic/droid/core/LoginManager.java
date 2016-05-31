package org.stepic.droid.core;

import android.content.Context;
import android.widget.Toast;

import com.yandex.metrica.YandexMetrica;

import org.stepic.droid.R;
import org.stepic.droid.preferences.SharedPreferenceHelper;
import org.stepic.droid.util.AppConstants;
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
    private final IShell mShell;
    private final Context mContext;

    @Inject
    public LoginManager(IShell shell, Context appContext) {
        mShell = shell;
        mContext = appContext;
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

    private void failLogin(Throwable t) {
        YandexMetrica.reportEvent(AppConstants.METRICA_FAIL_LOGIN);
        YandexMetrica.reportError(AppConstants.METRICA_FAIL_LOGIN, t);
        if (t != null) {
            if (t instanceof ProtocolException) {
                Toast.makeText(mContext, R.string.failLogin, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(mContext, R.string.connectionProblems, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void successLogin(Response<AuthenticationStepicResponse> response, ActivityFinisher finisher) {
        SharedPreferenceHelper preferenceHelper = mShell.getSharedPreferenceHelper();
        AuthenticationStepicResponse authStepic = response.body();
        preferenceHelper.storeAuthInfo(authStepic);

        if (authStepic != null) {
            YandexMetrica.reportEvent(AppConstants.METRICA_SUCCESS_LOGIN);
            mShell.getScreenProvider().showMainFeed(mContext);
            finisher.onFinish();
        } else {
            failLogin(new ProtocolException(JsonHelper.toJson(response.errorBody())));
        }
    }


}
