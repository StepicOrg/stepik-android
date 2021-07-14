package org.stepik.android.view.course_revenue.mapper

import android.content.Context
import android.text.SpannedString
import androidx.core.text.buildSpannedString
import androidx.core.text.scale
import org.stepic.droid.R
import java.util.Currency
import javax.inject.Inject

class RevenuePriceMapper
@Inject
constructor(
    private val context: Context
) {
    companion object {
        private const val RUB_FORMAT = "RUB"
    }
    fun mapToDisplayPrice(currencyCode: String, price: String, debitPrefixRequired: Boolean = false): SpannedString =
        when (currencyCode) {
            RUB_FORMAT -> {
                val first = price.substring(0, price.lastIndex - 1)
                val second = price.takeLast(2)

                context.getString(R.string.rub_format, price)
                buildSpannedString {
                    if (debitPrefixRequired) {
                        append("+")
                    }
                    append(first)
                    scale(0.85f) {
                        append(second)
                        append(" ")
                        append(Currency.getInstance(currencyCode).symbol)
                    }
                }
            }
            else ->
                buildSpannedString {
                    append("$price $currencyCode")
                }
        }
}