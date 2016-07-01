package org.stepic.droid.model

import com.google.gson.annotations.SerializedName

enum class UserRole private constructor(val value: String?) {
    @SerializedName("student")
    student("student"),
    @SerializedName("stuff")
    stuff("stuff"),
    @SerializedName("teacher")
    teacher("teacher")
}