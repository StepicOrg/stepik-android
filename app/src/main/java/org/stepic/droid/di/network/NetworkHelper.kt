package org.stepic.droid.di.network

import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.stepic.droid.jsonHelpers.adapters.CodeOptionsAdapterFactory
import org.stepic.droid.jsonHelpers.adapters.UTCDateAdapter
import org.stepic.droid.jsonHelpers.deserializers.DatasetDeserializer
import org.stepic.droid.jsonHelpers.deserializers.FeedbackDeserializer
import org.stepic.droid.jsonHelpers.deserializers.ReplyDeserializer
import org.stepic.droid.jsonHelpers.serializers.ReplySerializer
import org.stepik.android.model.ReplyWrapper
import org.stepik.android.model.attempts.DatasetWrapper
import org.stepik.android.model.feedback.Feedback
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Date
import java.util.concurrent.TimeUnit

object NetworkHelper {
    const val TIMEOUT_IN_SECONDS = 60L

    @JvmStatic
    fun createRetrofit(client: OkHttpClient, baseUrl: String): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .addConverterFactory(createGsonConverterFactory())
        .client(client)
        .build()


    @JvmStatic
    inline fun <reified T> createService(interceptors: Set<Interceptor>, host: String): T {
        val okHttpBuilder = OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
        interceptors.forEach { okHttpBuilder.addNetworkInterceptor(it) }
        val retrofit =
            createRetrofit(okHttpBuilder.build(), host)

        return retrofit.create(T::class.java)
    }

    @JvmStatic
    fun createGsonConverterFactory(): GsonConverterFactory =
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