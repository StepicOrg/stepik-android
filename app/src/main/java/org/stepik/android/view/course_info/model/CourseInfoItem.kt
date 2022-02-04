package org.stepik.android.view.course_info.model

import org.stepik.android.model.user.User
import org.stepik.android.view.video_player.model.VideoPlayerMediaData

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

    override fun hashCode(): Int =
        type.hashCode()

    override fun compareTo(other: CourseInfoItem): Int =
        type.ordinal.compareTo(other.type.ordinal)

    class AuthorsBlock(
        val authors: List<User?>
    ) : CourseInfoItem(CourseInfoType.AUTHORS)

    class Skills(
        val acquiredSkills: List<String>
    ) : CourseInfoItem(CourseInfoType.ACQUIRED_SKILLS)

    class SummaryBlock(
        val text: String
    ) : CourseInfoItem(CourseInfoType.SUMMARY)

    class AboutBlock(
        val text: String
    ) : CourseInfoItem(CourseInfoType.ABOUT)

    class OrganizationBlock(
        val organization: User
    ) : CourseInfoItem(CourseInfoType.ORGANIZATION)

    class VideoBlock(
        val videoMediaData: VideoPlayerMediaData
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