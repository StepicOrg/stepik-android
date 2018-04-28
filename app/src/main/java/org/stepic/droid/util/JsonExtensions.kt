package org.stepic.droid.util

import com.google.gson.Gson

inline fun <reified T> String.toObject(gson: Gson = Gson()): T =
        gson.fromJson(this, T::class.java)