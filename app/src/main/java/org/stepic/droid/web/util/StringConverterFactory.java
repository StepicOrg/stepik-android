package org.stepic.droid.web.util;

import okhttp3.RequestBody;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import retrofit2.Converter;

public class StringConverterFactory extends Converter.Factory {
    public static StringConverterFactory create() {
        return new StringConverterFactory();
    }

    private StringConverterFactory() {
    }

    public Converter<?, RequestBody> toRequestBody(Type type, Annotation[] annotations) {
        return StringRequestBodyConverter.INSTANCE;
    }
}
