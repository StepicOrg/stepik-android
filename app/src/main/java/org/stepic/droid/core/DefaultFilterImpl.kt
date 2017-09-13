package org.stepic.droid.core

import android.content.Context
import android.os.Build
import org.stepic.droid.model.StepikFilter
import org.stepic.droid.preferences.SharedPreferenceHelper
import java.util.*
import javax.inject.Inject

class DefaultFilterImpl
@Inject constructor(private val context: Context) : DefaultFilter {

    private var isNeedRussian: Boolean? = null
    private val SHOW_FILTER_FEATURE_WITH_LANGUAGE_RESOLVING = "before116"

    override fun getDefaultFeatured(filterValue: StepikFilter): Boolean =
            when (filterValue) {
                StepikFilter.RUSSIAN -> isNeedRussian()
                StepikFilter.ENGLISH -> true
                StepikFilter.UPCOMING -> true
                StepikFilter.ACTIVE -> true
                StepikFilter.PAST -> true
                StepikFilter.PERSISTENT -> false
            }

    override fun getDefaultEnrolled(filterValue: StepikFilter): Boolean =
            //my courses is persistent by default
            when (filterValue) {
                StepikFilter.RUSSIAN -> true
                StepikFilter.ENGLISH -> true
                StepikFilter.UPCOMING -> true
                StepikFilter.ACTIVE -> true
                StepikFilter.PAST -> true
                StepikFilter.PERSISTENT -> true
            }


    private fun isNeedRussian(): Boolean {
        val isNeedRussianNotNull = isNeedRussian ?: false
        if (isNeedRussian != null) {
            return isNeedRussianNotNull // do not change on language change, it is just helper for not russian speaker, choose performance.
        }

        if (!needResolveLanguage()) {
            val isNeedRussianLocal = true
            isNeedRussian = isNeedRussianLocal
            return isNeedRussianLocal // show russian always for old users (before 1.16)
        }

        val locale: Locale =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    context.resources.configuration.locales.get(0)
                } else {
                    @Suppress("DEPRECATION")
                    context.resources.configuration.locale
                }

        val newValue = locale.language == Locale("ru").language
        isNeedRussian = newValue
        return newValue
    }

    //avoid cyclic dependency
    override fun setNeedResolveLanguage() {
        put(SharedPreferenceHelper.PreferenceType.DEVICE_SPECIFIC, SHOW_FILTER_FEATURE_WITH_LANGUAGE_RESOLVING, true)
    }

    //avoid cyclic dependency
    private fun needResolveLanguage(): Boolean =
            getBoolean(SharedPreferenceHelper.PreferenceType.DEVICE_SPECIFIC, SHOW_FILTER_FEATURE_WITH_LANGUAGE_RESOLVING, false) //by default user before 116 and we don't show he/she only language courses #Apps-430

    //avoid cyclic dependency
    private fun getBoolean(preferenceType: SharedPreferenceHelper.PreferenceType, key: String, defaultValue: Boolean): Boolean =
            context.getSharedPreferences(preferenceType.storeName, Context.MODE_PRIVATE).getBoolean(key, defaultValue)

    //avoid cyclic dependency
    private fun put(type: SharedPreferenceHelper.PreferenceType, key: String, value: Boolean?) {
        val editor = context.getSharedPreferences(type.storeName, Context.MODE_PRIVATE).edit()
        editor.putBoolean(key, value!!).apply()
    }
}