package org.stepic.droid.util

import com.google.gson.Gson
import com.google.gson.JsonElement

inline fun <reified T> JsonElement.toObject(gson: Gson): T =
    gson.fromJson(this, T::class.java)

inline fun <reified T> String.toObject(gson: Gson = Gson()): T =
    gson.fromJson(this, T::class.java)