package org.stepic.droid.util

import org.stepic.droid.model.Course
import org.stepic.droid.model.DownloadingVideoItem

object  KotlinUtil {
    fun filterIfNotContains (list: List<DownloadingVideoItem>, set: Set<Long>) : List<DownloadingVideoItem>{
        val result = list.filter { !set.contains(it.downloadEntity.stepId) }
        return result
    }

    fun filterIfNotUnique(list:List<Course>) : List <Course> {
        val result = list.distinctBy { it.courseId }
        return result
    }
}