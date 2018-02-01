package org.stepic.droid.adaptive.util

object ExpHelper {
    @JvmStatic
    fun getCurrentLevel(exp: Long) =
            if (exp < 5)
                1
            else
                2 + (Math.log((exp / 5).toDouble()) / Math.log(2.0)).toLong()

    @JvmStatic
    fun getNextLevelExp(currentLevel: Long) =
            if (currentLevel == 1L)
                5
            else
                5 * Math.pow(2.0, (currentLevel - 1).toDouble()).toLong()
}