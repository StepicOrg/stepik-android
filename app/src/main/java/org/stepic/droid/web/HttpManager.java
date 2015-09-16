package org.stepic.droid.web;

import android.content.Context;
import android.os.Bundle;

import com.google.gson.JsonObject;
import com.squareup.okhttp.Authenticator;
import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.stepic.droid.base.MainApplication;
import org.stepic.droid.configuration.IConfig;
import org.stepic.droid.util.SharedPreferenceHelper;

import java.io.IOException;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class HttpManager implements IHttpManager {

    private final static int CONNECTION_TIMEOUT = 5000;
    private final static int READ_TIMEOUT = 5000;

    @Inject
    IConfig mConfig;

    @Inject
    SharedPreferenceHelper mSharedPreferencesHelper;
    @Inject
    Context mContext;

    OkHttpClient mOkHttpClient = new OkHttpClient();
    private static final MediaType DEFAULT_MEDIA_TYPE
            = MediaType.parse("application/x-www-form-urlencoded");

    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

    @Inject
    public HttpManager(Context context) {
        MainApplication.component(context).inject(this);
        mOkHttpClient.setConnectTimeout(CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);
        mOkHttpClient.setReadTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS);
    }

    @Override
    public String post(String url, Bundle params) throws IOException {

        String query = makeQueryFromBundle(params);
        setAuthenticatorClientIDAndPassword();

        RequestBody body = RequestBody.create(DEFAULT_MEDIA_TYPE, query);
        Request request = new Request.Builder()
                .post(body)
                .url(url)
                .build();

        Response response = mOkHttpClient.newCall(request).execute();

        return response.body().string();
    }

    @Override
    public String postJson(String url, JsonObject jsonObject) throws IOException {

        String credential = Credentials.basic(mConfig.getOAuthClientId(), mConfig.getOAuthClientSecret());//obsolete?

        String jsonStr = jsonObject.toString();
        RequestBody requestBody = RequestBody.create(JSON_MEDIA_TYPE, jsonStr);
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        Response response = mOkHttpClient.newCall(request).execute();

        String respStr = response.body().toString();

        return "";

    }

    @Override
    public String get(String baseUrl, Bundle params) throws IOException {
        AuthenticationStepicResponse resp = getAuthInfo();
        String access_token = resp.getAccess_token();
        String type = resp.getToken_type();

        String url = baseUrl + "?" + makeQueryFromBundle(params);
        String authValue = type + " " + access_token;
        //todo: keep in mind expiring of token
        Request request = new Request.Builder()
                .header("Authorization", authValue)
                .url(url)
                .get()
                .build();

        Response response = mOkHttpClient.newCall(request).execute();
        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

        return response.body().string();
    }


    private AuthenticationStepicResponse getAuthInfo() {
        AuthenticationStepicResponse response = mSharedPreferencesHelper.getAuthResponseFromStore(mContext);
        return response;
    }

    private String makeQueryFromBundle(Bundle params) {

        StringBuilder queryMaker = new StringBuilder();
        int i = 0;
        for (String key : params.keySet()) {
            i++;
            queryMaker.append(key);
            queryMaker.append('=');
            queryMaker.append(params.get(key));
            if (params.keySet().size() != i) {
                //if not last element
                queryMaker.append('&');
            }
        }
        return queryMaker.toString();
    }

    private void setAuthenticatorClientIDAndPassword() {
        mOkHttpClient.setAuthenticator(new Authenticator() {
            @Override
            public Request authenticate(Proxy proxy, Response response) throws IOException {
                String credential = Credentials.basic(mConfig.getOAuthClientId(), mConfig.getOAuthClientSecret());
                return response.request().newBuilder().header("Authorization", credential).build();
            }

            @Override
            public Request authenticateProxy(Proxy proxy, Response response) throws IOException {
                return null;
            }
        });
    }
}
