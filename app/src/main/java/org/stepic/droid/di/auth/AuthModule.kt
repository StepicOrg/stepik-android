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
import org.stepic.droid.di.network.NetworkHelper
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
        internal fun provideEmptyAuthService(config: Config): EmptyAuthService {
            val okHttpBuilder = OkHttpClient.Builder()
            okHttpBuilder.setTimeoutsInSeconds(NetworkHelper.TIMEOUT_IN_SECONDS)
            val retrofit = NetworkHelper.createRetrofit(
                okHttpBuilder.build(),
                config.baseUrl
            )
            return retrofit.create(EmptyAuthService::class.java)
        }

        @Provides
        @AppSingleton
        @JvmStatic
        @SocialAuthService
        internal fun provideSocialAuthService(
            userAgentProvider: UserAgentProvider,
            config: Config
        ): OAuthService =
            createAuthService(
                Credentials.basic(
                    config.getOAuthClientId(Api.TokenType.social), config.getOAuthClientSecret(
                        Api.TokenType.social
                    )
                ),
                userAgentProvider.provideUserAgent(),
                config.baseUrl
            )

        @Provides
        @AppSingleton
        @JvmStatic
        @AuthService
        internal fun provideAuthService(
            userAgentProvider: UserAgentProvider,
            config: Config
        ): OAuthService =
            createAuthService(
                Credentials.basic(
                    config.getOAuthClientId(Api.TokenType.loginPassword),
                    config.getOAuthClientSecret(
                        Api.TokenType.loginPassword
                    )
                ),
                userAgentProvider.provideUserAgent(),
                config.baseUrl
            )

        @Provides
        @AppSingleton
        @JvmStatic
        @CookieAuthService
        internal fun provideCookieAuthService(
            userAgentProvider: UserAgentProvider,
            cookieHelper: CookieHelper,
            config: Config
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
            okHttpBuilder.setTimeoutsInSeconds(NetworkHelper.TIMEOUT_IN_SECONDS)

            val retrofit = NetworkHelper.createRetrofit(
                okHttpBuilder.build(),
                config.baseUrl
            )
            return retrofit.create(OAuthService::class.java)
        }


        private fun createAuthService(credentials: String, userAgent: String, host: String): OAuthService {
            val okHttpBuilder = OkHttpClient.Builder()

            okHttpBuilder.addInterceptor { chain ->
                chain.proceed(chain.addUserAgent(userAgent).newBuilder().header(AppConstants.authorizationHeaderName, credentials).build())
            }
            okHttpBuilder.protocols(listOf(Protocol.HTTP_1_1))
            okHttpBuilder.setTimeoutsInSeconds(NetworkHelper.TIMEOUT_IN_SECONDS)

            val retrofit = NetworkHelper.createRetrofit(
                okHttpBuilder.build(),
                host
            )
            return retrofit.create(OAuthService::class.java)
        }
    }
}