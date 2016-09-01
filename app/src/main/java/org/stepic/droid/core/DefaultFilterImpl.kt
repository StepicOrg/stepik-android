package org.stepic.droid.core

import android.content.Context
import android.os.Build
import org.stepic.droid.model.StepikFilter
import java.util.*

class DefaultFilterImpl(private val context: Context) : DefaultFilter {

    private var isNeedRussian: Boolean? = null

    override fun getDefaultFeatured(filterValue: StepikFilter): Boolean {
        return getDefaultEnrolled(filterValue) //if we want to divide in the future
    }

    override fun getDefaultEnrolled(filterValue: StepikFilter): Boolean {
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


    private fun isNeedRussian(): Boolean {
        if (isNeedRussian != null) {
            return isNeedRussian!! // do not change on language change, it is just helper for not russian speaker, choose performance.
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
}