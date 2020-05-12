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

    private fun tryGetCsrfFromOnePair(keyValueCookie: String): String? =
        HttpCookie.parse(keyValueCookie).find { it.name == AppConstants.csrfTokenCookieName }?.value

    fun getCsrfTokenFromCookies(cookies: String): String {
        cookies.split(";").forEach {
            val csrf =
                tryGetCsrfFromOnePair(it)
            if (csrf != null) return csrf
        }
        return ""
    }

    fun removeCookiesCompat() {
        sharedPreferenceHelper.cookiesHeader = null
    }

    fun addCsrfTokenToRequest(request: Request): Request {
        val cookies = sharedPreferenceHelper
            .cookiesHeader
            ?.split(COOKIE_SEPARATOR)
            ?.flatMap { cookie ->
                HttpCookie.parse(cookie).filter { !it.hasExpired() }
            }
            ?: return request

        val csrftoken = cookies.find { it.name == AppConstants.csrfTokenCookieName }
            ?.value
            ?: ""

        val header = cookies.joinToString(separator = COOKIE_HEADER_SEPARATOR) { it.toString() }

        return request.newBuilder()
            .addHeader(AppConstants.refererHeaderName, config.baseUrl)
            .addHeader(AppConstants.csrfTokenHeaderName, csrftoken)
            .addHeader(AppConstants.cookieHeaderName, header)
            .build()
    }

    fun getCookiesForBaseUrl(): List<HttpCookie>? {
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

    fun updateCookieForBaseUrl() {
        val lang = Locale.getDefault().language
        val response = emptyAuthService.getStepicForFun(lang).execute()

        sharedPreferenceHelper.cookiesHeader =
            response.headers().values(AppConstants.setCookieHeaderName).joinToString(separator = COOKIE_SEPARATOR)
    }
}