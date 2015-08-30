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
    IConfig config;

    @Inject
    IHttpManager httpManager;

    @Override
    public IResponse authWithLoginPassword(String username, String password) {
        Bundle params = new Bundle();
        params.putString("grant_type", "password");
        params.putString("client_id", config.getOAuthClientId());
        params.putString("username", username);
        params.putString("password", password);

        String url = config.getBaseUrl() + "/oauth2/token/";

        String json = null;
        try {
            json = httpManager.post(url, params);
        } catch (IOException i) {
            //ignore
        }

        //todo: save to store
        Gson gson = new GsonBuilder().create();
        AuthenticationResponse res = gson.fromJson(json, AuthenticationResponse.class);

        return res;
    }
}
