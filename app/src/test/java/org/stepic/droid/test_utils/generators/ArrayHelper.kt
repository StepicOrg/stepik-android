package org.stepic.droid.test_utils.generators

object ArrayHelper {

    fun arrayOf(vararg a: Long): LongArray {
        val longArray = LongArray(a.size)

        a.forEachIndexed { i, value ->
            longArray[i] = value
        }

        return longArray
    }


}
