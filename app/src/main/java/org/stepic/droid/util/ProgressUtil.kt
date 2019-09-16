package org.stepic.droid.util

import org.stepik.android.model.Progressable

object ProgressUtil {
    fun getProgresses(objects: Iterable<Progressable>?): Array<String> {
        return objects
                ?.mapNotNull { it.progress }
                ?.toTypedArray()
                ?: emptyArray()
    }
}

@JvmName("progressableIterable_getProgresses")
fun Iterable<Progressable>.getProgresses(): Array<String> =
    ProgressUtil.getProgresses(this)
