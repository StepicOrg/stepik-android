package org.stepic.droid.testUtils.generators

object ArrayHelper {

    fun arrayOf(vararg a: Long): LongArray {
        val longArray = LongArray(a.size)

        a.forEachIndexed { i, value ->
            longArray[i] = value
        }

        return longArray
    }


}
