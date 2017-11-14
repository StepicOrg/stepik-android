package org.stepic.droid.util

import org.stepic.droid.model.IProgressable
import org.stepic.droid.model.Progress

object ProgressUtil {
    fun <T : IProgressable> getAllProgresses(objects: List<T>?): Array<String> {
        return objects
                ?.mapNotNull { it.getProgressId() }
                ?.toTypedArray()
                ?: emptyArray<String>()
    }

    fun getProgressPercent(progress: Progress?): Int? {
        if (progress == null) {
            return null
        }

        val score: Double? = progress.score?.let { StringUtil.safetyParseString(it) }
        val cost = progress.cost
        return if (score != null) {
            val progressPart: Double = score / cost
            val progressShow: Int = (progressPart * 100).toInt()
            progressShow
        } else {
            null
        }
    }
}
