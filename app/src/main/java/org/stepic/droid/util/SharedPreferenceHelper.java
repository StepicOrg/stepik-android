package org.stepic.droid.util;

import android.content.Context;
import android.content.SharedPreferences;

import org.stepic.droid.web.AuthenticationStepicResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javax.inject.Singleton;

@Singleton
public class SharedPreferenceHelper {



    public enum PreferenceType {
        LOGIN("login preference");

        private String description;

        PreferenceType(String description) {
            this.description = description;
        }

        private String getStoreName() {
            return description;
        }
    }

    public void storeAuthInfo(Context context, AuthenticationStepicResponse response) {
        Gson gson = new Gson();
        String json = gson.toJson(response);
        put(PreferenceType.LOGIN, AUTH_RESPONSE_JSON, json, context);

    }

    public void deleteAuthInfo(Context context) {
        clear(PreferenceType.LOGIN, context);
    }

    public AuthenticationStepicResponse getAuthResponseFromStore(Context context) {
        String json = getString(PreferenceType.LOGIN, AUTH_RESPONSE_JSON, context);
        if (json == null) {
            return null;
        }

        Gson gson = new GsonBuilder().create();
        AuthenticationStepicResponse result = gson.fromJson(json, AuthenticationStepicResponse.class);
        return result;
    }


    private void put(PreferenceType type, String key, String value, Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(type.getStoreName(), Context.MODE_PRIVATE).edit();
        editor.putString(key, value).apply();
    }

    private void clear(PreferenceType type, Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(type.getStoreName(), Context.MODE_PRIVATE).edit();
        editor.clear().apply();
    }


    private String getString(PreferenceType preferenceType, String key, Context context) {
        return context.getSharedPreferences(preferenceType.getStoreName(), Context.MODE_PRIVATE)
                .getString(key, null);
    }


    private final String AUTH_RESPONSE_JSON = "auth_response_json";

}
