package org.stepic.droid.di.auth

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import org.stepic.droid.configuration.Config
import org.stepic.droid.di.AppSingleton
import org.stepic.droid.di.network.AuthLock
import org.stepic.droid.di.network.AuthService
import org.stepic.droid.di.network.CookieAuthService
import org.stepic.droid.di.network.NetworkFactory
import org.stepic.droid.di.network.SocialAuthService
import org.stepic.droid.features.auth.repository.AuthRepository
import org.stepic.droid.features.auth.repository.AuthRepositoryImpl
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.CookieHelper
import org.stepic.droid.util.addUserAgent
import org.stepic.droid.util.setTimeoutsInSeconds
import org.stepic.droid.web.Api
import org.stepic.droid.web.AuthInterceptor
import org.stepic.droid.web.EmptyAuthService
import org.stepic.droid.web.OAuthService
import org.stepic.droid.web.UserAgentProvider
import retrofit2.Converter
import java.util.concurrent.locks.ReentrantLock

@Module
abstract class AuthModule {

    @Binds
    @IntoSet
    internal abstract fun bindAuthInterceptor(authInterceptor: AuthInterceptor): Interceptor

    @Binds
    @AppSingleton
    abstract fun bindAuthRepository(authRepositoryImpl: AuthRepositoryImpl): AuthRepository

    @Module
    companion object {

        @Provides
        @AppSingleton
        @JvmStatic
        @AuthLock
        internal fun provideAuthLock(): ReentrantLock = ReentrantLock()

        @Provides
        @AppSingleton
        @JvmStatic
        internal fun provideEmptyAuthService(config: Config, converterFactory: Converter.Factory): EmptyAuthService {
            val okHttpBuilder = OkHttpClient.Builder()
            okHttpBuilder.setTimeoutsInSeconds(NetworkFactory.TIMEOUT_IN_SECONDS)
            val retrofit = NetworkFactory.createRetrofit(
                config.baseUrl,
                okHttpBuilder.build(),
                converterFactory
            )
            return retrofit.create(EmptyAuthService::class.java)
        }

        @Provides
        @AppSingleton
        @JvmStatic
        @SocialAuthService
        internal fun provideSocialAuthService(
            config: Config,
            userAgentProvider: UserAgentProvider,
            converterFactory: Converter.Factory
        ): OAuthService =
            createAuthService(
                Credentials.basic(
                    config.getOAuthClientId(Api.TokenType.social), config.getOAuthClientSecret(
                        Api.TokenType.social
                    )
                ),
                userAgentProvider.provideUserAgent(),
                config.baseUrl,
                converterFactory
            )

        @Provides
        @AppSingleton
        @JvmStatic
        @AuthService
        internal fun provideAuthService(
            config: Config,
            userAgentProvider: UserAgentProvider,
            converterFactory: Converter.Factory
        ): OAuthService =
            createAuthService(
                Credentials.basic(
                    config.getOAuthClientId(Api.TokenType.loginPassword),
                    config.getOAuthClientSecret(Api.TokenType.loginPassword)
                ),
                userAgentProvider.provideUserAgent(),
                config.baseUrl,
                converterFactory
            )

        @Provides
        @AppSingleton
        @JvmStatic
        @CookieAuthService
        internal fun provideCookieAuthService(
            config: Config,
            userAgentProvider: UserAgentProvider,
            cookieHelper: CookieHelper,
            converterFactory: Converter.Factory
        ): OAuthService {
            val okHttpBuilder = OkHttpClient.Builder()
            okHttpBuilder.addNetworkInterceptor { chain ->
                cookieHelper.removeCookiesCompat()
                cookieHelper.updateCookieForBaseUrl()
                chain.proceed(
                    cookieHelper.addCsrfTokenToRequest(
                        chain.addUserAgent(userAgentProvider.provideUserAgent())
                    )
                )
            }
            okHttpBuilder.setTimeoutsInSeconds(NetworkFactory.TIMEOUT_IN_SECONDS)

            val retrofit = NetworkFactory.createRetrofit(config.baseUrl, okHttpBuilder.build(), converterFactory)
            return retrofit.create(OAuthService::class.java)
        }


        private fun createAuthService(credentials: String, userAgent: String, host: String, converterFactory: Converter.Factory): OAuthService {
            val okHttpBuilder = OkHttpClient.Builder()

            okHttpBuilder.addInterceptor { chain ->
                chain.proceed(chain.addUserAgent(userAgent).newBuilder().header(AppConstants.authorizationHeaderName, credentials).build())
            }
            okHttpBuilder.protocols(listOf(Protocol.HTTP_1_1))
            okHttpBuilder.setTimeoutsInSeconds(NetworkFactory.TIMEOUT_IN_SECONDS)

            val retrofit = NetworkFactory.createRetrofit(host, okHttpBuilder.build(), converterFactory)
            return retrofit.create(OAuthService::class.java)
        }
    }
}