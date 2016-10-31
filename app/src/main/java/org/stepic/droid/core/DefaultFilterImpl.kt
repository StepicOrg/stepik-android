package org.stepic.droid.core

import android.content.Context
import android.os.Build
import org.stepic.droid.model.StepikFilter
import org.stepic.droid.preferences.SharedPreferenceHelper
import java.util.*

class DefaultFilterImpl(private val context: Context) : DefaultFilter {

    private var isNeedRussian: Boolean? = null
    private val SHOW_FILTER_FEATURE_WITH_LANGUAGE_RESOLVING = "before116"

    override fun getDefaultFeatured(filterValue: StepikFilter): Boolean {
        when (filterValue) {
            StepikFilter.RUSSIAN -> return isNeedRussian()
            StepikFilter.ENGLISH -> return true
            StepikFilter.UPCOMING -> return true
            StepikFilter.ACTIVE -> return true
            StepikFilter.PAST -> return true
            StepikFilter.PERSISTENT -> return false
            else -> return false
        }
    }

    override fun getDefaultEnrolled(filterValue: StepikFilter): Boolean {
        //my courses is persistent by default
        when (filterValue) {
            StepikFilter.RUSSIAN -> return true
            StepikFilter.ENGLISH -> return true
            StepikFilter.UPCOMING -> return true
            StepikFilter.ACTIVE -> return true
            StepikFilter.PAST -> return true
            StepikFilter.PERSISTENT -> return true
            else -> return false
        }
    }


    private fun isNeedRussian(): Boolean {
        if (isNeedRussian != null) {
            return isNeedRussian!! // do not change on language change, it is just helper for not russian speaker, choose performance.
        }

        if (!needResolveLanguage()) {
            isNeedRussian = true
            return isNeedRussian!! // show russian always for old users (before 1.16)
        }

        val locale: Locale
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = context.resources.configuration.locales.get(0)
        } else {
            locale = context.resources.configuration.locale
        }

        isNeedRussian = locale.language == Locale("ru").language
        return isNeedRussian!!
    }

    //avoid cyclic dependency
    override fun setNeedResolveLanguage() {
        put(SharedPreferenceHelper.PreferenceType.DEVICE_SPECIFIC, SHOW_FILTER_FEATURE_WITH_LANGUAGE_RESOLVING, true)
    }

    //avoid cyclic dependency
    private fun needResolveLanguage(): Boolean {
        return getBoolean(SharedPreferenceHelper.PreferenceType.DEVICE_SPECIFIC, SHOW_FILTER_FEATURE_WITH_LANGUAGE_RESOLVING, false) //by default user before 116 and we don't show he/she only language courses #Apps-430
    }

    //avoid cyclic dependency
    private fun getBoolean(preferenceType: SharedPreferenceHelper.PreferenceType, key: String, defaultValue: Boolean): Boolean {
        return context.getSharedPreferences(preferenceType.getStoreName(), Context.MODE_PRIVATE).getBoolean(key, defaultValue)
    }

    //avoid cyclic dependency
    private fun put(type: SharedPreferenceHelper.PreferenceType, key: String, value: Boolean?) {
        val editor = context.getSharedPreferences(type.getStoreName(), Context.MODE_PRIVATE).edit()
        editor.putBoolean(key, value!!).apply()
    }
}