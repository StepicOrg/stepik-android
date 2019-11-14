package org.stepik.android.remote.base

import okhttp3.OkHttpClient
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory

object NetworkFactory {
    const val TIMEOUT_IN_SECONDS = 60L

    @JvmStatic
    fun createRetrofit(baseUrl: String, client: OkHttpClient, converterFactory: Converter.Factory): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(converterFactory)
            .client(client)
            .build()


    @JvmStatic
    inline fun <reified T> createService(host: String, okHttpClient: OkHttpClient, converterFactory: Converter.Factory): T {
        val retrofit =
            createRetrofit(
                host,
                okHttpClient,
                converterFactory
            )
        return retrofit.create(T::class.java)
    }
}