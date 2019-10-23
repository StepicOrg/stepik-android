package org.stepik.android.model

import com.google.gson.annotations.SerializedName

enum class UserRole {
    @SerializedName("student")
    STUDENT,
    @SerializedName("staff")
    STAFF,
    @SerializedName("teacher")
    TEACHER
}