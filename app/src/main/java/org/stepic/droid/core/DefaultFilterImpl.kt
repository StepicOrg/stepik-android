package org.stepic.droid.core

import android.content.Context
import android.os.Build
import org.stepic.droid.model.StepikFilter
import java.util.*
import javax.inject.Inject

class DefaultFilterImpl
@Inject
constructor(private val context: Context) : DefaultFilter {

    private var isNeedRussian: Boolean? = null

    override fun getDefaultFilter(filterValue: StepikFilter, needResolveLanguage: Boolean): Boolean =
            when (filterValue) {
                StepikFilter.RUSSIAN -> isNeedRussian(needResolveLanguage)
                StepikFilter.ENGLISH -> true
            }


    private fun isNeedRussian(needResolveLanguage: Boolean): Boolean {
        val isNeedRussianNotNull = isNeedRussian ?: false
        if (isNeedRussian != null) {
            return isNeedRussianNotNull // do not change on language change, it is just helper for not russian speaker, choose performance.
        }

        if (!needResolveLanguage) {
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
}