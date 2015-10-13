package org.stepic.droid.web;

import android.content.Context;

import com.google.gson.JsonObject;

import org.stepic.droid.configuration.IConfig;
import org.stepic.droid.util.SharedPreferenceHelper;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

@Deprecated
@Singleton
public class Api {
    Context mContext;

    @Inject
    public Api(Context context) {
        mContext = context;
//        MainApplication.component(context).inject(this);
    }

    @Inject
    IConfig mConfig;

    @Inject
    IHttpManager mHttpManager;

    @Inject
    SharedPreferenceHelper mSharedPreferencesHelper;

    public IStepicResponse signUp(String firstName, String secondName, String email, String password) {
// FIXME: 02.10.15 Registration doesn't work
        JsonObject innerObject = new JsonObject();
        innerObject.addProperty("first_name", firstName);
        innerObject.addProperty("last_name", secondName);
        innerObject.addProperty("email", email);
        innerObject.addProperty("password", password);

        JsonObject jsonObject = new JsonObject();
        jsonObject.add("user", innerObject);


        String url = mConfig.getBaseUrl() + "/api/users/";
        //todo implement registration

        String json = null;
        try {
            json = mHttpManager.postJson(url, jsonObject).body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }

        int test = 9000;
        return null;
    }


}
