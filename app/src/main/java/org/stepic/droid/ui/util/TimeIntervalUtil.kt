package org.stepic.droid.ui.util

import java.util.*

object TimeIntervalUtil {
    val values: Array<String>
    val middle: Int

    init {
        val numberOfHours = 24
        val valuesList = ArrayList<String>(numberOfHours)
        for (i in 0 until numberOfHours) {
            valuesList.add(String.format("%02d:00 - %02d:00", i, i + 1))
        }
        values = valuesList.toTypedArray()
        middle = values.size / 2
    }
}
