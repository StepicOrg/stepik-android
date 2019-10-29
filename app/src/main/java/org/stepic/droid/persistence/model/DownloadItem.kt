package org.stepic.droid.persistence.model

import org.stepik.android.model.Course
import ru.nobird.android.core.model.Identifiable

data class DownloadItem(
    val course: Course,
    val status: DownloadProgress.Status
): Identifiable<Long> {
    override val id: Long
        get() = course.id
}