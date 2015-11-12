package org.stepic.droid.util;

import com.google.gson.Gson;

public class JsonHelper {
    public static String toJson(Object obj) {
        if (obj == null) return "";
        Gson gson = new Gson();

        // convert java object to JSON format,
        // and returned as JSON formatted string
        String json = gson.toJson(obj);
        return json;
    }
}
