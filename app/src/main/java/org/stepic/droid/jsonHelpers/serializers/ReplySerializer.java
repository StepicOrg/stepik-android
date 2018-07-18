package org.stepic.droid.jsonHelpers.serializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import org.stepik.android.model.learning.Reply;
import org.stepik.android.model.learning.ReplyWrapper;

import java.lang.reflect.Type;

public class ReplySerializer implements JsonSerializer<ReplyWrapper> {
    @Override
    public JsonElement serialize(ReplyWrapper src, Type typeOfSrc, JsonSerializationContext context) {
        Reply reply = src.getReply();
        if (reply.getTableChoices() == null) {
            return context.serialize(reply);
        } else {
            JsonElement tableChoicesJsonElement = context.serialize(reply.getTableChoices(), reply.getTableChoices().getClass());
            reply.setTableChoices(null);
            JsonElement replyJsonElement = context.serialize(reply);
            JsonObject replyJsonObject = replyJsonElement.getAsJsonObject();
            replyJsonObject.add("choices", tableChoicesJsonElement);
            return replyJsonObject;
        }
    }
}
