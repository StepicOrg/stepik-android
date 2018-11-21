package org.stepik.android.view.course_info.model

import org.stepik.android.model.Video
import org.stepik.android.model.user.User

sealed class CourseInfoItem(
        val type: CourseInfoType
) : Comparable<CourseInfoItem> {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CourseInfoItem

        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int = type.hashCode()

    override fun compareTo(other: CourseInfoItem): Int =
            type.ordinal.compareTo(other.type.ordinal)


    class OrganizationBlock(
            val organizationTitle: String
    ) : CourseInfoItem(CourseInfoType.ORGANIZATION)

    class VideoBlock(
            val video: Video
    ) : CourseInfoItem(CourseInfoType.VIDEO)

    sealed class WithTitle(type: CourseInfoType) : CourseInfoItem(type) {
        class TextBlock(
                type: CourseInfoType,
                val text: String
        ) : WithTitle(type)

        class InstructorsBlock(
                val instructors: List<User?>
        ) : WithTitle(CourseInfoType.INSTRUCTORS)
    }
}