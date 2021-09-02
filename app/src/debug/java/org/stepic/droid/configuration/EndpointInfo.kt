package org.stepic.droid.configuration

import com.google.gson.annotations.SerializedName

data class EndpointInfo(
    @SerializedName("api_host_url")
    val apiHostUrl: String,
    @SerializedName("oauth_client_id")
    val oauthClientId: String,
    @SerializedName("oauth_client_secret")
    val oauthClientSecret: String,
    @SerializedName("oauth_client_id_social")
    val oauthClientIdSocial: String,
    @SerializedName("oauth_client_secret_social")
    val oauthClientSecretSocial: String
)