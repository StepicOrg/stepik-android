package org.stepic.droid.model

data class DbVideoUrl(
        val videoId: Long,
        val quality: String?,
        val url: String?
)
