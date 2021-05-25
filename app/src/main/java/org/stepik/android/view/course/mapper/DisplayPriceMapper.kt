package org.stepik.android.view.course.mapper

import android.content.Context
import org.stepic.droid.R
import javax.inject.Inject

class DisplayPriceMapper
@Inject
constructor(
    private val context: Context
) {
    companion object {
        private const val RUB_FORMAT = "RUB"
        private const val USD_FORMAT = "USD"
    }
    fun mapToDisplayPrice(currencyCode: String, price: String): String =
        when (currencyCode) {
            RUB_FORMAT ->
                context.getString(R.string.rub_format, price.substringBefore('.'))
            USD_FORMAT ->
                context.getString(R.string.usd_format, price)
            else ->
                "$price $currencyCode"
        }
}