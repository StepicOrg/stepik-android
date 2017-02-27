package org.stepic.droid.web.util;

import okhttp3.MediaType;
import okhttp3.RequestBody;

import java.io.IOException;

import retrofit2.Converter;


public class StringRequestBodyConverter<T> implements Converter<T, RequestBody> {
    static final StringRequestBodyConverter<Object> INSTANCE = new StringRequestBodyConverter<>();
    private static final MediaType MEDIA_TYPE = MediaType.parse("text/plain; charset=UTF-8");

    private StringRequestBodyConverter() {
    }

    @Override
    public RequestBody convert(T value) throws IOException {
        return RequestBody.create(MEDIA_TYPE, String.valueOf(value));
    }
}