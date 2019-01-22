package org.stepic.droid.configuration

import android.content.Context
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import org.stepic.droid.di.AppSingleton
import org.stepic.droid.web.Api
import java.io.InputStreamReader
import java.nio.charset.Charset
import javax.inject.Inject

@AppSingleton
class ConfigImpl
private constructor() : Config {

    @AppSingleton
    class ConfigFactory
    @Inject
    constructor(private val context: Context) {
        fun create(): ConfigImpl {
            return context.assets.open("configs/config.json").use {
                val gson = GsonBuilder()
                        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                        .create()
                gson.fromJson(InputStreamReader(it, Charset.defaultCharset()), ConfigImpl::class.java)
            }
        }
    }

    private val apiHostUrl: String? = null
    private val oauthClientId: String? = null
    private val oauthClientSecret: String? = null
    private val grantType: String? = null
    private val oauthClientIdSocial: String? = null
    private val oauthClientSecretSocial: String? = null
    private val grantTypeSocial: String? = null
    private val refreshGrantType: String? = null
    private val redirectUri: String? = null
    private val zendeskHost: String? = null
    private val courseCloseable: Boolean = false
    private val customUpdate: Boolean = false
    private val updateEndpoint: String? = null
    private val firebaseDomain: String? = null
    private val googleServerClientId: String? = null
    private val termsOfService: String? = null
    private val privacyPolicy: String? = null
    private val csrfCookieName: String? = null
    private val sessionCookieName: String? = null
    private val amplitudeApiKey: String? = null
    private val publicLicenseKey: String? = null
    private val isAppInStore: Boolean = false

    override fun getOAuthClientId(type: Api.TokenType) = when (type) {
        Api.TokenType.social -> oauthClientIdSocial
        Api.TokenType.loginPassword -> oauthClientId
    }

    override fun getBaseUrl() = apiHostUrl

    override fun getOAuthClientSecret(type: Api.TokenType) = when (type) {
        Api.TokenType.social -> oauthClientSecretSocial
        Api.TokenType.loginPassword -> oauthClientSecret
    }

    override fun getGrantType(type: Api.TokenType) = when (type) {
        Api.TokenType.social -> grantTypeSocial
        Api.TokenType.loginPassword -> grantType
    }

    override fun getRefreshGrantType() = refreshGrantType

    override fun getRedirectUri() = redirectUri

    override fun getZendeskHost() = zendeskHost

    override fun isUserCanDropCourse() = courseCloseable

    override fun isCustomUpdateEnable() = customUpdate

    override fun getUpdateEndpoint() = updateEndpoint

    override fun getFirebaseDomain() = firebaseDomain

    override fun getGoogleServerClientId() = googleServerClientId

    override fun getPrivacyPolicyUrl() = privacyPolicy

    override fun getTermsOfServiceUrl() = termsOfService

    override fun getCsrfTokenCookieName() = csrfCookieName

    override fun getSessionCookieName() = sessionCookieName

    override fun isAppInStore() = isAppInStore

    override fun getAmplitudeApiKey() = amplitudeApiKey

    override fun getAppPublicLicenseKey() = publicLicenseKey
}
