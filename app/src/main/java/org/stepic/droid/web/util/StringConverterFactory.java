package org.stepic.droid.web.util;

import com.squareup.okhttp.RequestBody;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import retrofit.Converter;

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
