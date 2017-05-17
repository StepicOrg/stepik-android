package org.stepic.droid.util

import android.os.Build

fun isKitKatOrLater() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

fun isNougatOrLater() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N

fun isJellyBeanOrLater() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN

