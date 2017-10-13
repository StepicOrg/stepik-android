package org.stepic.droid.jsonHelpers.deserializers;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import org.stepic.droid.model.Dataset;
import org.stepic.droid.model.DatasetWrapper;

import java.lang.reflect.Type;

public class DatasetDeserializer implements JsonDeserializer<DatasetWrapper> {
    @Override
    public DatasetWrapper deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (!(json instanceof JsonObject)) {
            try {
                Object o = context.deserialize(json, String.class);
                Dataset dataset = new Dataset((String) o);
                return new DatasetWrapper(dataset);
            } catch (Exception e) {
                //if it is primitive, but not string.
                return new DatasetWrapper();
            }
        } else {
            return new DatasetWrapper((Dataset) context.deserialize(json, Dataset.class));
        }
    }
}
