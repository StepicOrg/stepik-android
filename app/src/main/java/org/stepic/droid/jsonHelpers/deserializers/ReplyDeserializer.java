package org.stepic.droid.jsonHelpers.deserializers;


import android.support.annotation.NonNull;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import org.stepik.android.model.Reply;
import org.stepik.android.model.ReplyWrapper;
import org.stepik.android.model.TableChoiceAnswer;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ReplyDeserializer implements JsonDeserializer<ReplyWrapper> {


    @Override
    public ReplyWrapper deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        try {
            JsonObject reply = json.getAsJsonObject();
            if (reply.has("choices")) {
                JsonArray choiceElements = reply.get("choices").getAsJsonArray();
                int size = choiceElements.size();
                List<TableChoiceAnswer> tableChoiceAnswerList = new ArrayList<>();
                if (size != 0) {
                    for (int i = 0; i < size; i++) {
                        JsonElement tableChoiceElement = choiceElements.get(i);
                        TableChoiceAnswer tableChoiceAnswer = context.deserialize(tableChoiceElement, TableChoiceAnswer.class);
                        tableChoiceAnswerList.add(tableChoiceAnswer);
                    }
                }
                json.getAsJsonObject().remove("choices");
                Reply originReply = context.deserialize(json, Reply.class);
                originReply.setTableChoices(tableChoiceAnswerList);
                return new ReplyWrapper(originReply);
            } else {
                return deserializeAsReply(json, context);
            }

        } catch (Exception exception) {
            return deserializeAsReply(json, context);
        }

    }

    // if we do not find table choices -> deserialize as Reply
    @NonNull
    private ReplyWrapper deserializeAsReply(JsonElement json, JsonDeserializationContext context) {
        return new ReplyWrapper((Reply) context.deserialize(json, Reply.class));
    }
}