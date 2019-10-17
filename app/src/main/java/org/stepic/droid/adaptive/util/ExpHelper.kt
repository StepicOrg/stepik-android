package org.stepic.droid.adaptive.util

import kotlin.math.ln
import kotlin.math.pow

object ExpHelper {
    @JvmStatic
    fun getCurrentLevel(exp: Long): Long =
        if (exp < 5)
            1
        else
            2 + (ln((exp / 5).toDouble()) / ln(2.0)).toLong()

    @JvmStatic
    fun getNextLevelExp(currentLevel: Long): Long =
        if (currentLevel == 1L)
            5
        else
            5 * 2.0.pow((currentLevel - 1).toDouble()).toLong()
}