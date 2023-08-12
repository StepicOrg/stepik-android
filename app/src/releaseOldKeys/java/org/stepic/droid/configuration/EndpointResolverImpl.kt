package org.stepic.droid.configuration

import org.stepik.android.remote.auth.model.TokenType
import javax.inject.Inject

class EndpointResolverImpl
@Inject
constructor(
    private val config: Config
) : EndpointResolver {
    override fun getOAuthClientId(type: TokenType): String =
        config.getOAuthClientId(type)

    override fun getBaseUrl(): String =
        config.baseUrl

    override fun getOAuthClientSecret(type: TokenType): String =
        config.getOAuthClientSecret(type)
}