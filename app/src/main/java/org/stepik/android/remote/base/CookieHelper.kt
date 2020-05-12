package org.stepik.android.remote.base

import okhttp3.Request
import org.stepic.droid.configuration.Config
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.util.AppConstants
import org.stepik.android.remote.auth.service.EmptyAuthService
import java.net.HttpCookie
import java.net.URI
import java.net.URISyntaxException
import java.util.Locale
import javax.inject.Inject

class CookieHelper
@Inject
constructor(
    private val sharedPreferenceHelper: SharedPreferenceHelper,
    private val emptyAuthService: EmptyAuthService,
    private val config: Config
) {
    companion object {
        private const val COOKIE_SEPARATOR = "|"
        private const val COOKIE_HEADER_SEPARATOR = ";"
    }

    fun getCsrfTokenFromCookies(cookies: List<HttpCookie>): String =
        cookies.find { it.name == AppConstants.csrfTokenCookieName }
            ?.value
            ?: ""

    fun removeCookiesCompat() {
        sharedPreferenceHelper.cookiesHeader = null
    }

    fun addCsrfTokenToRequest(request: Request): Request {
        val cookies = getCookiesForBaseUrl()
            ?: return request

        val csrftoken = getCsrfTokenFromCookies(cookies)

        val header = getCookieHeader(cookies)

        return request.newBuilder()
            .addHeader(AppConstants.refererHeaderName, config.baseUrl)
            .addHeader(AppConstants.csrfTokenHeaderName, csrftoken)
            .addHeader(AppConstants.cookieHeaderName, header)
            .build()
    }

    fun getFreshCookiesForBaseUrl(): List<HttpCookie>? {
        val lang = Locale.getDefault().language
        val response = emptyAuthService.getStepicForFun(lang).execute()

        val cookieManager = java.net.CookieManager()
        val myUri: URI
        try {
            myUri = URI(config.baseUrl)
        } catch (e: URISyntaxException) {
            return null
        }

        cookieManager.put(myUri, response.headers().toMultimap())
        return cookieManager.cookieStore.get(myUri)
    }

    fun getCookiesForBaseUrl(): List<HttpCookie>? =
        sharedPreferenceHelper
            .cookiesHeader
            ?.split(COOKIE_SEPARATOR)
            ?.let(::getCookiesFromHeader)
            ?.takeIf { it.isNotEmpty() }

    fun fetchCookiesForBaseUrl(): List<HttpCookie> {
        val lang = Locale.getDefault().language
        val response = emptyAuthService.getStepicForFun(lang).execute()
        val header = response.headers().values(AppConstants.setCookieHeaderName)

        sharedPreferenceHelper.cookiesHeader =
            header.joinToString(separator = COOKIE_SEPARATOR)

        return getCookiesFromHeader(header)
    }

    private fun getCookiesFromHeader(header: List<String>): List<HttpCookie> =
        header.flatMap { cookie -> HttpCookie.parse(cookie).filter { !it.hasExpired() } }

    fun getCookieHeader(cookies: List<HttpCookie>): String =
        cookies.joinToString(separator = COOKIE_HEADER_SEPARATOR) { it.toString() }
}