package org.stepic.droid.util

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

private const val USER_AGENT_NAME = "User-Agent"

fun OkHttpClient.Builder.setTimeoutsInSeconds(timeout: Long) {
    connectTimeout(timeout, TimeUnit.SECONDS)
    readTimeout(timeout, TimeUnit.SECONDS)
}

fun Interceptor.Chain.addUserAgent(userAgent: String): Request =
    this.request()
        .newBuilder()
        .header(USER_AGENT_NAME, userAgent)
        .build()