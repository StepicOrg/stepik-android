package org.stepic.droid.web;

import android.os.Bundle;

import com.google.gson.JsonObject;
import com.squareup.okhttp.Response;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;

@Deprecated
public interface IHttpManager {
    String post(String url, Bundle params) throws IOException;

    //todo: change this architecture to universal post
    Response postJson(String url, JsonObject jsonObject) throws IOException;

    Response postJson(String url, String jsonString) throws IOException;

    String get(String url, @Nullable Bundle params) throws IOException;
}
