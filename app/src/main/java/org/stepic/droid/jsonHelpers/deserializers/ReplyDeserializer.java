package org.stepic.droid.jsonHelpers.deserializers;


import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;

import org.stepik.android.model.Reply;
import org.stepik.android.model.ReplyWrapper;

import java.lang.reflect.Type;

public class ReplyDeserializer implements JsonDeserializer<ReplyWrapper> {
    @Override
    public ReplyWrapper deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        return new ReplyWrapper(context.deserialize(json, Reply.class));
    }
}