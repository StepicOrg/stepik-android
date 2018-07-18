package org.stepic.droid.testUtils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.stepic.droid.jsonHelpers.adapters.CodeOptionsAdapterFactory
import org.stepic.droid.jsonHelpers.deserializers.DatasetDeserializer
import org.stepic.droid.jsonHelpers.deserializers.ReplyDeserializer
import org.stepic.droid.jsonHelpers.serializers.ReplySerializer
import org.stepik.android.model.learning.attempts.DatasetWrapper
import org.stepik.android.model.learning.ReplyWrapper

object TestingGsonProvider {
    val gson: Gson by lazy {
        GsonBuilder()
                .enableComplexMapKeySerialization()
                .registerTypeAdapterFactory(CodeOptionsAdapterFactory())
                .registerTypeAdapter(DatasetWrapper::class.java, DatasetDeserializer())
                .registerTypeAdapter(ReplyWrapper::class.java, ReplyDeserializer())
                .registerTypeAdapter(ReplyWrapper::class.java, ReplySerializer())
                .create()
    }
}
