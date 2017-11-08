package org.stepic.droid.model

import java.util.*

data class CourseListItem(
        private val id: Long,
        private val position: Int,
        val title: String,
        private val language: String,
        val courses: LongArray,
        val description: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CourseListItem

        if (id != other.id) return false
        if (position != other.position) return false
        if (title != other.title) return false
        if (language != other.language) return false
        if (!Arrays.equals(courses, other.courses)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + position
        result = 31 * result + title.hashCode()
        result = 31 * result + language.hashCode()
        result = 31 * result + Arrays.hashCode(courses)
        return result
    }
}