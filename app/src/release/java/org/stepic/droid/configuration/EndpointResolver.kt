package org.stepic.droid.configuration

import org.stepik.android.remote.auth.model.TokenType

interface EndpointResolver {
    fun getOAuthClientId(type: TokenType): String
    fun getBaseUrl(): String
    fun getOAuthClientSecret(type: TokenType): String
}