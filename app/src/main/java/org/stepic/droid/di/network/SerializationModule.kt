package org.stepic.droid.di.network

import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import org.stepic.droid.di.AppSingleton
import org.stepic.droid.jsonHelpers.adapters.CodeOptionsAdapterFactory
import org.stepic.droid.jsonHelpers.adapters.UTCDateAdapter
import org.stepic.droid.jsonHelpers.deserializers.DatasetDeserializer
import org.stepic.droid.jsonHelpers.deserializers.FeedbackDeserializer
import org.stepic.droid.jsonHelpers.deserializers.ReplyDeserializer
import org.stepic.droid.jsonHelpers.serializers.ReplySerializer
import org.stepik.android.model.ReplyWrapper
import org.stepik.android.model.attempts.DatasetWrapper
import org.stepik.android.model.feedback.Feedback
import retrofit2.Converter
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Date

@Module
abstract class SerializationModule {
    @Module
    companion object {
        @Provides
        @AppSingleton
        @JvmStatic
        internal fun provideGsonConverterFactory(): Converter.Factory =
            GsonConverterFactory.create(
                GsonBuilder()
                    .enableComplexMapKeySerialization()
                    .registerTypeAdapterFactory(CodeOptionsAdapterFactory())
                    .registerTypeAdapter(DatasetWrapper::class.java, DatasetDeserializer())
                    .registerTypeAdapter(ReplyWrapper::class.java, ReplyDeserializer())
                    .registerTypeAdapter(ReplyWrapper::class.java, ReplySerializer())
                    .registerTypeAdapter(Date::class.java, UTCDateAdapter())
                    .registerTypeAdapter(Feedback::class.java, FeedbackDeserializer())
                    .create()
            )
    }
}