package org.stepic.droid.persistence.model

class DownloadTask(
        val originalPath: String,
        val course: Long,
        val section: Long,
        val unit: Long,
        val lesson: Long,
        val step: Long
)