package org.stepic.droid.web.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.RequestBody;
import retrofit2.Converter;

public final class StringConverterFactory extends Converter.Factory {
    public static StringConverterFactory create() {
        return new StringConverterFactory();
    }

    private StringConverterFactory() {
    }

    public Converter<?, RequestBody> toRequestBody(Type type, Annotation[] annotations) {
        return StringRequestBodyConverter.INSTANCE;
    }
}
