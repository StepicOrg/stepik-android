package org.stepic.droid.features.course.ui.model.course_content

import org.stepic.droid.persistence.model.DownloadProgress
import org.stepik.android.model.Lesson
import org.stepik.android.model.Progress
import org.stepik.android.model.Section
import org.stepik.android.model.Unit

sealed class CourseContentItem {
    object ControlBar : CourseContentItem()

    data class SectionItem(
            val section: Section,
            val progress: Progress,
            val downloadProgress: DownloadProgress
    ) : CourseContentItem()

    data class LessonItem(
            val section: Section,
            val unit: Unit,
            val lesson: Lesson,
            val progress: Progress,
            val downloadProgress: DownloadProgress
    ) : CourseContentItem()
}