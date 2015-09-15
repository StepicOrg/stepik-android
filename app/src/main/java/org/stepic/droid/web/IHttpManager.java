package org.stepic.droid.web;

import android.os.Bundle;

import com.google.gson.JsonObject;

import java.io.IOException;

public interface IHttpManager {
    String post(String url, Bundle params) throws IOException;

    //todo: change this architecture to universal post
    String postJson(String url, JsonObject jsonObject) throws IOException;
}
