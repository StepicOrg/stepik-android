package com.elpatika.stepic.web;

import android.os.Bundle;

import com.elpatika.stepic.configuration.IConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.io.IOException;

@Singleton
public class Api implements IApi {

    @Inject
    IConfig mConfig;

    @Inject
    IHttpManager httpManager;

    @Override
    public IResponse authWithLoginPassword(String username, String password) {
        Bundle params = new Bundle();
        params.putString("grant_type", mConfig.getGrantType());
        params.putString("username", username);
        params.putString("password", password);

        String url = mConfig.getBaseUrl() + "/oauth2/token/";

        String json = null;
        try {
            json = httpManager.post(url, params);
        } catch (IOException i) {
            //ignore
        }

        //todo: save to store
        Gson gson = new GsonBuilder().create();

        return gson.fromJson(json, AuthenticationResponse.class);
    }

    @Override
    public IResponse signUp(String firstName, String secondName, String email, String password) {
        Bundle params = new Bundle();
        params.putString("first_name", firstName);
        params.putString("last_name", secondName);
        params.putString("email", email);
        params.putString("password", password);


        String url = mConfig.getBaseUrl() + "/accounts/signup/";
        //todo implement registration

        return null;
    }


}
