package org.stepic.droid.test_utils.generators

object ListHelper {
    fun <T> listOf(vararg a: T): List<T> {
        return a.toList()
    }

}
