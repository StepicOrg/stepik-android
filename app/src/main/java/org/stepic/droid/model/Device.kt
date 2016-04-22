package org.stepic.droid.model

import com.google.gson.annotations.SerializedName

data class Device(
        var id: Long = 0,
        var registration_id: String? = null,
        var user: Long = 0,
        var description: String? = null,
        var client_type: ClientType
)

enum class ClientType {
    @SerializedName("android")
    Android,
    @SerializedName("ios")
    iOS
}