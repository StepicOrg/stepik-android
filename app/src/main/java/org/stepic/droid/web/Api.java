package org.stepic.droid.web;

import android.content.Context;
import android.os.Bundle;

import org.stepic.droid.base.MainApplication;
import org.stepic.droid.configuration.IConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class Api implements IApi {
    @Inject
    public Api (Context context) {
        MainApplication.component(context).inject(this);
    }

    @Inject
    IConfig mConfig;

    @Inject
    IHttpManager mHttpManager;

    @Override
    public IStepicResponse authWithLoginPassword(String username, String password) {
        Bundle params = new Bundle();
        params.putString("grant_type", mConfig.getGrantType());
        params.putString("username", username);
        params.putString("password", password);

        String url = mConfig.getBaseUrl() + "/oauth2/token/";

        String json = null;
        try {
            json = mHttpManager.post(url, params);
        } catch (IOException i) {
            //ignore
            //Too many follow-up requests: 21 when incorrect user/password
        }

        Gson gson = new GsonBuilder().create();

        return gson.fromJson(json, AuthenticationStepicResponse.class);
    }

    @Override
    public IStepicResponse signUp(String firstName, String secondName, String email, String password) {

        JsonObject innerObject = new JsonObject();
        innerObject.addProperty("first_name", firstName);
        innerObject.addProperty("last_name", secondName);
        innerObject.addProperty("email", email);
        innerObject.addProperty("password", password);

        JsonObject jsonObject= new JsonObject();
        jsonObject.add("user", innerObject);



        String url = mConfig.getBaseUrl() + "/api/users/";
        //todo implement registration

        String json = null;
        try {
            json = mHttpManager.postJson(url, jsonObject);
        } catch (IOException e) {
            e.printStackTrace();
        }

        int test = 9000;
        return null;
    }


}
