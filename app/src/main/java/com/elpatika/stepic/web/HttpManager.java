package com.elpatika.stepic.web;

import android.os.Bundle;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Set;

public class HttpManager implements IHttpManager {

    OkHttpClient mOkHttpClient = new OkHttpClient();
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    @Override
    public String post(String url, Bundle params) throws IOException {

        JSONObject json = new JSONObject();
        Set<String> keys = params.keySet();
        for (String key : keys) {
            try {
                 json.put(key, params.get(key));// see edit below
//                json.put(key, JSONObject.wrap(params.get(key)));
            } catch(JSONException e) {
                //Handle exception here
            }
        }

        String jsonForQuery = json.toString();

        RequestBody body = RequestBody.create(JSON, jsonForQuery);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        Response response = mOkHttpClient.newCall(request).execute();
        return response.body().string();
    }
}
