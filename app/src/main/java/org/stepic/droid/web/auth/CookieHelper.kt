//package org.stepic.droid.web.auth
//
//import android.os.Build
//import android.os.Looper
//import android.webkit.CookieManager
//import io.reactivex.Completable
//import io.reactivex.Scheduler
//import okhttp3.Request
//import org.stepic.droid.configuration.Config
//import org.stepic.droid.di.AppSingleton
//import org.stepic.droid.di.qualifiers.BackgroundScheduler
//import org.stepic.droid.util.AppConstants
//import java.net.HttpCookie
//import java.net.URI
//import java.net.URISyntaxException
//import java.util.*
//import javax.inject.Inject
//
//@AppSingleton
//class CookieHelper
//@Inject constructor(
//        private val config: Config,
//        private val emptyAuthService: EmptyAuthService,
//        private val cookieManager: CookieManager,
//        @BackgroundScheduler
//        private val backgroundScheduler: Scheduler
//)  {
//    companion object {
//        private fun tryGetCsrfFromOnePair(keyValueCookie: String): String? =
//                HttpCookie.parse(keyValueCookie).find { it.name == AppConstants.csrfTokenCookieName }?.value
//
//        private fun getCsrfTokenFromCookies(cookies: String): String {
//            cookies.split(";").forEach {
//                val csrf = tryGetCsrfFromOnePair(it)
//                if (csrf != null) return csrf
//            }
//            return ""
//        }
//    }
//
//    fun addCsrfTokenToRequest(request: Request): Request {
//        val cookies = cookieManager.getCookie(config.baseUrl)
//        return if (cookies == null) {
//            request
//        } else {
//            val csrftoken = getCsrfTokenFromCookies(cookies)
//            request.newBuilder()
//                    .addHeader(AppConstants.refererHeaderName, config.baseUrl)
//                    .addHeader(AppConstants.csrfTokenHeaderName, csrftoken)
//                    .addHeader(AppConstants.cookieHeaderName, cookies)
//                    .build()
//        }
//    }
//
//    fun getCookiesForBaseUrl(): List<HttpCookie>? {
//        val lang = Locale.getDefault().language
//        val response = emptyAuthService.getStepicForFun(lang).execute()
//
//        val cookieManager = java.net.CookieManager()
//        val myUri: URI
//        try {
//            myUri = URI(config.baseUrl)
//        } catch (e: URISyntaxException) {
//            return null
//        }
//
//        cookieManager.put(myUri, response.headers().toMultimap())
//        return cookieManager.cookieStore.get(myUri)
//    }
//
//    fun updateCookieForBaseUrl() {
//        val lang = Locale.getDefault().language
//        val response = emptyAuthService.getStepicForFun(lang).execute()
//
//        val setCookieHeaders = response.headers().values(AppConstants.setCookieHeaderName)
//        for (value in setCookieHeaders) {
//            if (value != null) {
//                cookieManager.setCookie(config.baseUrl, value) //set-cookie is not empty
//            }
//        }
//    }
//
//    fun removeCookiesCompat() {
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
//            @Suppress("DEPRECATION")
//            CookieManager.getInstance().removeAllCookie()
//        } else {
//            CookieManager.getInstance().removeAllCookies(null)
//        }
//    }
//}