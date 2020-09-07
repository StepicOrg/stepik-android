package org.stepik.android.view.injection.auth

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
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.DebugToolsHelper
import org.stepic.droid.util.addUserAgent
import org.stepic.droid.util.setTimeoutsInSeconds
import org.stepik.android.data.auth.repository.AuthRepositoryImpl
import org.stepik.android.data.auth.source.AuthRemoteDataSource
import org.stepik.android.domain.auth.repository.AuthRepository
import org.stepik.android.remote.auth.AuthRemoteDataSourceImpl
import org.stepik.android.remote.auth.interceptor.AuthInterceptor
import org.stepik.android.remote.auth.model.TokenType
import org.stepik.android.remote.auth.service.EmptyAuthService
import org.stepik.android.remote.auth.service.OAuthService
import org.stepik.android.remote.base.CookieHelper
import org.stepik.android.remote.base.NetworkFactory
import org.stepik.android.remote.base.UserAgentProvider
import org.stepik.android.view.injection.qualifiers.AuthLock
import org.stepik.android.view.injection.qualifiers.AuthService
import org.stepik.android.view.injection.qualifiers.CookieAuthService
import org.stepik.android.view.injection.qualifiers.SocialAuthService
import retrofit2.Converter
import java.util.concurrent.locks.ReentrantReadWriteLock

@Module
abstract class AuthDataModule {

    @Binds
    @IntoSet
    internal abstract fun bindAuthInterceptor(authInterceptor: AuthInterceptor): Interceptor

    @Binds
    @AppSingleton
    abstract fun bindAuthRepository(authRepositoryImpl: AuthRepositoryImpl): AuthRepository

    @Binds
    internal abstract fun bindAuthRemoteDataSource(authRemoteDataSourceImpl: AuthRemoteDataSourceImpl): AuthRemoteDataSource

    @Module
    companion object {
        private val debugInterceptors = DebugToolsHelper.getDebugInterceptors()

        private fun addDebugInterceptors(okHttpBuilder: OkHttpClient.Builder) {
            debugInterceptors.forEach { okHttpBuilder.addNetworkInterceptor(it) }
        }

        @Provides
        @AppSingleton
        @JvmStatic
        @AuthLock
        internal fun provideAuthLock(): ReentrantReadWriteLock =
            ReentrantReadWriteLock()

        @Provides
        @AppSingleton
        @JvmStatic
        internal fun provideEmptyAuthService(
            config: Config,
            userAgentProvider: UserAgentProvider,
            converterFactory: Converter.Factory
        ): EmptyAuthService {
            val okHttpBuilder = OkHttpClient.Builder()
            okHttpBuilder.setTimeoutsInSeconds(NetworkFactory.TIMEOUT_IN_SECONDS)
            addDebugInterceptors(okHttpBuilder)
            okHttpBuilder.addInterceptor { chain ->
                chain.proceed(chain.addUserAgent(userAgentProvider.provideUserAgent()))
            }
            val retrofit = NetworkFactory.createRetrofit(config.baseUrl, okHttpBuilder.build(), converterFactory)
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
                Credentials.basic(config.getOAuthClientId(TokenType.SOCIAL), config.getOAuthClientSecret(TokenType.SOCIAL)),
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
                    config.getOAuthClientId(TokenType.LOGIN_PASSWORD),
                    config.getOAuthClientSecret(TokenType.LOGIN_PASSWORD)
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
                cookieHelper.fetchCookiesForBaseUrl()
                chain.proceed(
                    cookieHelper.addCsrfTokenToRequest(
                        chain.addUserAgent(userAgentProvider.provideUserAgent())
                    )
                )
            }
            okHttpBuilder.setTimeoutsInSeconds(NetworkFactory.TIMEOUT_IN_SECONDS)
            addDebugInterceptors(okHttpBuilder)

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
            addDebugInterceptors(okHttpBuilder)

            val retrofit = NetworkFactory.createRetrofit(host, okHttpBuilder.build(), converterFactory)
            return retrofit.create(OAuthService::class.java)
        }
    }
}