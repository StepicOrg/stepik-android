package com.elpatika.stepic.web;

import android.os.Bundle;

import com.elpatika.stepic.configuration.IConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.io.IOException;

@Singleton
public class Api implements IApi {

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
        }

        //todo: save to store
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
