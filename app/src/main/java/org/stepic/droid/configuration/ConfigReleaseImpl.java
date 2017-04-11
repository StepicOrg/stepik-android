package org.stepic.droid.configuration;

import android.content.Context;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.stepic.droid.di.AppSingleton;
import org.stepic.droid.web.Api;

import java.io.InputStream;
import java.io.InputStreamReader;

import javax.inject.Inject;

@AppSingleton
public class ConfigReleaseImpl implements Config {

    private JsonObject properties;

    private static final String API_HOST_URL = "API_HOST_URL";
    private static final String OAUTH_CLIENT_ID = "OAUTH_CLIENT_ID";
    private static final String OAUTH_CLIENT_SECRET = "OAUTH_CLIENT_SECRET";
    private static final String GRANT_TYPE = "GRANT_TYPE";
    private static final String OAUTH_CLIENT_ID_SOCIAL = "OAUTH_CLIENT_ID_SOCIAL";
    private static final String OAUTH_CLIENT_SECRET_SOCIAL = "OAUTH_CLIENT_SECRET_SOCIAL";
    private static final String GRANT_TYPE_SOCIAL = "GRANT_TYPE_SOCIAL";
    private static final String REFRESH_GRANT_TYPE = "REFRESH_GRANT_TYPE";
    private static final String REDIRECT_URI = "REDIRECT_URI";
    private static final String ZENDESK_HOST = "ZENDESK_HOST";
    private static final String COURSE_CLOSEABLE = "COURSE_CLOSEABLE";
    private static final String CUSTOM_UPDATE = "CUSTOM_UPDATE";
    private static final String UPDATE_ENDPOINT = "UPDATE_ENDPOINT";
    private static final String FIREBASE_DOMAIN = "FIREBASE_DOMAIN";
    private static final String GOOGLE_SERVER_CLIENT_ID = "GOOGLE_SERVER_CLIENT_ID";
    private static final String TERMS_OF_SERVICE = "TERMS_OF_SERVICE";
    private static final String PRIVACY_POLICY = "PRIVACY_POLICY";
    private static final String MIXPANEL_TOKEN = "MIXPANEL_TOKEN";
    private static final String CSRF_COOKIE_NAME = "CSRF_COOKIE_NAME";
    private static final String SESSION_COOKIE_NAME = "SESSION_COOKIE_NAME";
    private static final String IS_APP_IN_STORE = "IS_APP_IN_STORE";


    @Inject
    public ConfigReleaseImpl(Context context) {
        try {
            InputStream in = context.getAssets().open("configs/config.json");
            JsonParser parser = new JsonParser();
            JsonElement config = parser.parse(new InputStreamReader(in));
            properties = config.getAsJsonObject();
        } catch (Exception e) {
            properties = new JsonObject();
        }
    }

    @Override
    public String getOAuthClientId(Api.TokenType type) {
        switch (type) {
            case social:
                return getString(OAUTH_CLIENT_ID_SOCIAL);
            case loginPassword:
                return getString(OAUTH_CLIENT_ID);
            default:
                return null;
        }
    }

    @Override
    public String getBaseUrl() {
        return getString(API_HOST_URL);
    }

    @Override
    public String getOAuthClientSecret(Api.TokenType type) {
        switch (type) {
            case social:
                return getString(OAUTH_CLIENT_SECRET_SOCIAL);
            case loginPassword:
                return getString(OAUTH_CLIENT_SECRET);
            default:
                return null;
        }
    }

    @Override
    public String getGrantType(Api.TokenType type) {
        switch (type) {
            case social:
                return getString(GRANT_TYPE_SOCIAL);
            case loginPassword:
                return getString(GRANT_TYPE);
            default:
                return null;
        }
    }

    @Override
    public String getRefreshGrantType() {
        return getString(REFRESH_GRANT_TYPE);
    }

    @Override
    public String getRedirectUri() {
        return getString(REDIRECT_URI);
    }

    @Override
    public String getZendeskHost() {
        return getString(ZENDESK_HOST);
    }

    @Override
    public boolean isUserCanDropCourse() {
        return getBoolean(COURSE_CLOSEABLE, true);
    }

    @Override
    public boolean isCustomUpdateEnable() {
        return getBoolean(CUSTOM_UPDATE, false);
    }

    @Override
    public String getUpdateEndpoint() {
        return getString(UPDATE_ENDPOINT, "");
    }

    @Override
    public String getFirebaseDomain() {
        return getString(FIREBASE_DOMAIN, null);
    }

    @Override
    public String getGoogleServerClientId() {
        return getString(GOOGLE_SERVER_CLIENT_ID, null);
    }

    @Override
    public String getPrivacyPolicyUrl() {
        return getString(PRIVACY_POLICY, getBaseUrl());
    }

    @Override
    public String getTermsOfServiceUrl() {
        return getString(TERMS_OF_SERVICE, getBaseUrl());
    }

    @Override
    public String getMixpanelToken() {
        return getString(MIXPANEL_TOKEN, null);
    }

    @Override
    public String getCsrfTokenCookieName() {
        return getString(CSRF_COOKIE_NAME, null);
    }

    @Override
    public String getSessionCookieName() {
        return getString(SESSION_COOKIE_NAME, null);
    }

    @Override
    public boolean isAppInStore() {
        return getBoolean(IS_APP_IN_STORE, false);
    }

    private String getString(String key) {
        return getString(key, null);
    }

    private String getString(String key, String defaultValue) {
        JsonElement element = getObject(key);
        if (element != null) {
            return element.getAsString();
        } else {
            return defaultValue;
        }
    }

    private boolean getBoolean(String key, boolean defaultValue) {
        JsonElement element = getObject(key);
        if (element != null) {
            return element.getAsBoolean();
        } else {
            return defaultValue;
        }
    }

    private JsonElement getObject(String key) {
        return properties.get(key);
    }
}
