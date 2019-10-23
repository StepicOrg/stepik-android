package org.stepic.droid.core

import android.content.Context
import org.stepic.droid.model.StepikFilter
import org.stepic.droid.util.defaultLocale
import java.util.Locale
import javax.inject.Inject

class DefaultFilterImpl
@Inject
constructor(private val context: Context) : DefaultFilter {

    private var isNeedRussian: Boolean? = null

    override fun getDefaultFilter(filterValue: StepikFilter): Boolean =
            when (filterValue) {
                StepikFilter.RUSSIAN -> isNeedRussian()
                StepikFilter.ENGLISH -> !isNeedRussian()
            }


    private fun isNeedRussian(): Boolean {
        val isNeedRussianNotNull = isNeedRussian ?: false
        if (isNeedRussian != null) {
            return isNeedRussianNotNull // do not change on language change, it is just helper for not russian speaker, choose performance.
        }

        val locale: Locale = context.resources.configuration.defaultLocale

        val newValue = locale.language == Locale("ru").language
        isNeedRussian = newValue
        return newValue
    }
}