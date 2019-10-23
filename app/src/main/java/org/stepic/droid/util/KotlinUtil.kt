package org.stepic.droid.util

import org.stepik.android.model.Course
import java.util.ArrayList
import java.util.HashSet

object KotlinUtil {
    fun getListOldPlusUpdated(oldList: List<Course>, newList: List<Course>) =
        mergeTwoCourseList(newList = newList, oldList = oldList)

    //oldList should be first, and after that newList, but if exists 2 elements with the same ID, get from newList at position of oldList
    private fun mergeTwoCourseList(oldList: List<Course>, newList: List<Course>): List<Course> {
        val hashMap = newList.associateBy { it.id }
        val usedFromNew = HashSet<Long>(newList.size)
        val result = ArrayList<Course>(newList.size + oldList.size)
        oldList.forEach {
            val updatedCourse: Course? = hashMap[it.id]
            if (updatedCourse != null) {
                result.add(updatedCourse)
                usedFromNew.add(updatedCourse.id)
            } else {
                result.add(it)
            }
        }

        //do not add used from new
        newList.forEach {
            if (!usedFromNew.contains(it.id)) {
                result.add(it)
            }
        }

        return result
    }

    inline fun <T> setIfNot(setter: (T) -> Unit, value: T, not: T) {
        if (value != not) {
            setter(value)
        }
    }
}