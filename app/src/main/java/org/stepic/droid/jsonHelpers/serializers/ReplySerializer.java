package org.stepic.droid.jsonHelpers.serializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import org.stepik.android.model.ReplyWrapper;

import java.lang.reflect.Type;

public class ReplySerializer implements JsonSerializer<ReplyWrapper> {
    @Override
    public JsonElement serialize(ReplyWrapper src, Type typeOfSrc, JsonSerializationContext context) {
        return context.serialize(src.getReply());
    }
}
