package org.stepic.droid.ui.util

import java.util.ArrayList

object TimeIntervalUtil {
    val values: Array<String>
    val defaultTimeCode: Int

    init {
        val numberOfHours = 24
        val valuesList = ArrayList<String>(numberOfHours)
        for (i in 0 until numberOfHours) {
            valuesList.add(String.format("%02d:00 \u2014 %02d:00", i, i + 1))
        }
        values = valuesList.toTypedArray()
        defaultTimeCode = 20 // most usable
    }
}
