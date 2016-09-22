package org.stepic.droid.util

import org.stepic.droid.model.Course
import org.stepic.droid.model.DownloadingVideoItem
import java.text.DecimalFormat
import java.util.*

object KotlinUtil {
    fun filterIfNotContains(list: List<DownloadingVideoItem>, set: Set<Long>): List<DownloadingVideoItem> {
        val result = list.filter { !set.contains(it.downloadEntity.stepId) }
        return result
    }

    fun filterIfNotUnique(list: List<Course>): List <Course> {
        val result = list.distinctBy { it.courseId }
        return result
    }

    fun getListOldPlusUpdated(oldList: List<Course>, newList: List<Course>): List<Course> {
        val result = ArrayList<Course>(newList.size + oldList.size)
        result.addAll(newList)
        result.addAll(oldList)
        return result.distinctBy { it.courseId }
    }

    fun getNiceFormatOfDouble(number: Double): String {
        val format = DecimalFormat("0.##");
        return format.format(number)
    }
}