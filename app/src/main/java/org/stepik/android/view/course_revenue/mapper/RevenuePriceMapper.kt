package org.stepik.android.view.course_revenue.mapper

import android.content.Context
import org.stepic.droid.R
import javax.inject.Inject

class RevenuePriceMapper
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
            RUB_FORMAT -> {
                context.getString(R.string.rub_format, price)
            }

            USD_FORMAT ->
                context.getString(R.string.usd_format, price)
            else ->
                "$price $currencyCode"
        }
}