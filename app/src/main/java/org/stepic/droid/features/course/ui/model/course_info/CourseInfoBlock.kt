package org.stepic.droid.features.course.ui.model.course_info

abstract class CourseInfoBlock(
        val type: CourseInfoType
): Comparable<CourseInfoBlock> {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CourseInfoBlock

        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int = type.hashCode()

    override fun compareTo(other: CourseInfoBlock): Int =
            type.ordinal.compareTo(other.type.ordinal)
}