package org.stepik.android.view.progress.ui.mapper

import android.content.Context
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import org.stepic.droid.R
import org.stepic.droid.util.toFixed

object ProgressTextMapper {
    fun mapProgressToText(context: Context, stepScore: Float, stepCost: Long, @StringRes stringRes: Int, @StringRes fractionRes: Int, @PluralsRes pluralRes: Int): String {
        val decimalsCount = context.resources.getInteger(R.integer.score_decimal_count)

        return if (stepScore.toLong() == 0L) {
            context.getString(fractionRes, stepScore.toFixed(decimalsCount), stepCost)
        } else {
            val quantity = context.resources.getQuantityString(pluralRes, stepScore.toInt(), stepScore.toFixed(decimalsCount))
            context.getString(stringRes, quantity, stepCost)
        }
    }
}