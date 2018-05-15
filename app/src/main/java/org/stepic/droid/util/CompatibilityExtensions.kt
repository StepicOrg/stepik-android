package org.stepic.droid.util

import android.content.res.Configuration
import android.os.Build
import java.util.*

val Configuration.defaultLocale: Locale
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        locales.get(0)
    } else {
        @Suppress("DEPRECATION")
        locale
    }