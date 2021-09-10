package org.stepik.android.domain.debug.model

data class DebugSettings(
    val fcmToken: String,
    val currentEndpointConfig: EndpointConfig,
    val endpointConfigSelection: Int
)
