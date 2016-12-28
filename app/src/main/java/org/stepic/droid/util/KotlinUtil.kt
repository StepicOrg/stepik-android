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

    fun getListOldPlusUpdated(oldList: List<Course>, newList: List<Course>)
            = mergeTwoCourseList(newList = newList, oldList = oldList)

    fun getNiceFormatOfDouble(number: Double): String {
        val format = DecimalFormat("0.##");
        return format.format(number)
    }


    //oldList should be first, and after that newList, but if exists 2 elements with the same ID, get from newList at position of oldList
    private fun mergeTwoCourseList(oldList: List<Course>, newList: List<Course>): List<Course> {
        val hashMap = newList.associateBy { it.courseId }
        val usedFromNew = HashSet<Long>(newList.size)
        val result = ArrayList<Course>(newList.size + oldList.size)
        oldList.forEach {
            val updatedCourse: Course? = hashMap[it.courseId]
            if (updatedCourse != null) {
                result.add(updatedCourse)
                usedFromNew.add(updatedCourse.courseId)
            } else {
                result.add(it)
            }
        }

        //do not add used from new
        newList.forEach {
            if (!usedFromNew.contains(it.courseId)) {
                result.add(it)
            }
        }

        return result
    }
}