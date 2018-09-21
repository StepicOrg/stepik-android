package org.stepic.droid.model

import com.google.gson.annotations.SerializedName

class Device(
        var id: Long = 0,
        var registration_id: String? = null,
        var user: Long = 0,
        var description: String? = null,
        @SerializedName("client_type")
        var clientType: ClientType,
        @SerializedName("is_badges_enabled")
        val isBadgesEnabled: Boolean?
)

enum class ClientType {
    @SerializedName("android")
    Android,
    @SerializedName("ios")
    iOS
}