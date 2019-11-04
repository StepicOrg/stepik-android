package org.stepic.droid.util

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

fun OkHttpClient.Builder.setTimeoutsInSeconds(timeout: Long) {
    connectTimeout(timeout, TimeUnit.SECONDS)
    readTimeout(timeout, TimeUnit.SECONDS)
}