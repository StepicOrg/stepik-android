package org.stepik.android.view.course_info.model

import org.stepik.android.model.user.User
import org.stepik.android.view.video_player.model.VideoPlayerMediaData
import ru.nobird.app.core.model.Identifiable

sealed class CourseInfoItem(
    open val type: CourseInfoType
) : Comparable<CourseInfoItem>, Identifiable<Int> {
    override val id: Int
        get() = type.ordinal

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

    data class AuthorsBlock(
        val authors: List<User?>
    ) : CourseInfoItem(CourseInfoType.AUTHORS)

    data class Skills(
        val acquiredSkills: List<String>
    ) : CourseInfoItem(CourseInfoType.ACQUIRED_SKILLS)

    data class SummaryBlock(
        val text: String
    ) : CourseInfoItem(CourseInfoType.SUMMARY)

    data class AboutBlock(
        val text: String
    ) : CourseInfoItem(CourseInfoType.ABOUT)

    data class VideoBlock(
        val videoMediaData: VideoPlayerMediaData
    ) : CourseInfoItem(CourseInfoType.VIDEO)

    sealed class WithTitle(type: CourseInfoType) : CourseInfoItem(type) {
        data class TextBlock(
            override val type: CourseInfoType,
            val text: String
        ) : WithTitle(type)

        data class InstructorsBlock(
            val instructors: List<User?>
        ) : WithTitle(CourseInfoType.INSTRUCTORS)
    }
}