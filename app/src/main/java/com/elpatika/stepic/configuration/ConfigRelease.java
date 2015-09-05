package com.elpatika.stepic.configuration;

import android.content.Context;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.io.InputStream;
import java.io.InputStreamReader;

@Singleton
public class ConfigRelease implements IConfig {

    private JsonObject mProperties;

    private static final String API_HOST_URL = "API_HOST_URL";
    private static final String OAUTH_CLIENT_ID = "OAUTH_CLIENT_ID";
    private static final String OAUTH_CLIENT_SECRET = "OAUTH_CLIENT_SECRET";
    private static final String GRANT_TYPE = "GRANT_TYPE";


    @Inject
    public ConfigRelease (Context context) {
        try {
            InputStream in = context.getAssets().open("configs/config.json");
            JsonParser parser = new JsonParser();
            JsonElement config = parser.parse(new InputStreamReader(in));
            mProperties = config.getAsJsonObject();
        } catch (Exception e) {
            mProperties = new JsonObject();
        }
    }




    @Override
    public String getOAuthClientId() {
        return getString(OAUTH_CLIENT_ID);
    }

    @Override
    public String getBaseUrl() {
        return getString(API_HOST_URL);
    }

    @Override
    public String getOAuthClientSecret() {
        return getString(OAUTH_CLIENT_SECRET);
    }

    @Override
    public String getGrantType() {
        return getString(GRANT_TYPE);
    }

    private String getString(String key) {
        return getString(key, null);
    }

    private String getString(String key, String defaultValue) {
        JsonElement element = getObject(key);
        if(element != null) {
            return element.getAsString();
        }
        else {
            return defaultValue;
        }
    }

    private JsonElement getObject(String key) {
        return mProperties.get(key);
    }
}
