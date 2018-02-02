package org.stepic.droid.util

import android.content.res.Resources
import android.os.Build
import java.util.*

fun getLocale(resources: Resources): Locale =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            resources.configuration.locales.get(0)
        } else {
            @Suppress("DEPRECATION")
            resources.configuration.locale
        }
