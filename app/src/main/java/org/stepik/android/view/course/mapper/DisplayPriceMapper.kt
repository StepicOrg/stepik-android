package org.stepik.android.view.course.mapper

import android.content.Context
import android.text.SpannedString
import androidx.core.text.buildSpannedString
import androidx.core.text.scale
import androidx.core.text.strikeThrough
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

    fun mapToDiscountedDisplayPriceSpannedString(originalDisplayPrice: String, currencyCode: String, promoPrice: String): SpannedString {
        val promoDisplayPrice = mapToDisplayPrice(currencyCode, promoPrice)
        return buildSpannedString {
            append(context.getString(R.string.course_payments_purchase_in_web_with_price_promo))
            append(promoDisplayPrice)
            append(" ")
            scale(0.9f) {
                strikeThrough {
                    append(originalDisplayPrice)
                }
            }
        }
    }

    fun mapToDiscountedDisplayPriceSpannedStringMobileTiers(originalDisplayPrice: String, promoPrice: String): SpannedString =
        buildSpannedString {
            append(context.getString(R.string.course_payments_purchase_in_web_with_price_promo))
            append(promoPrice)
            append(" ")
            scale(0.9f) {
                strikeThrough {
                    append(originalDisplayPrice)
                }
            }
        }
}