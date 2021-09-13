package org.stepic.droid.configuration

import org.stepik.android.remote.auth.model.TokenType
import javax.inject.Inject

class EndpointResolverImpl
@Inject
constructor(
    endpointInfoFactory: EndpointInfoFactory
) : EndpointResolver {

    private val endpointInfo: EndpointInfo = endpointInfoFactory.createEndpointInfo()

    override fun getOAuthClientId(type: TokenType): String =
        when (type) {
            TokenType.SOCIAL -> endpointInfo.oauthClientIdSocial
            TokenType.LOGIN_PASSWORD -> endpointInfo.oauthClientId
        }

    override fun getBaseUrl(): String =
        endpointInfo.apiHostUrl

    override fun getOAuthClientSecret(type: TokenType): String =
        when (type) {
            TokenType.SOCIAL -> endpointInfo.oauthClientSecretSocial
            TokenType.LOGIN_PASSWORD -> endpointInfo.oauthClientSecret
        }
}