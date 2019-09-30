package org.stepic.droid.persistence.model

import org.stepik.android.model.Course

data class DownloadItem(
    val course: Course,
    val status: DownloadProgress.Status
)