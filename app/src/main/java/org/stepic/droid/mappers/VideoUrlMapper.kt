package org.stepic.droid.mappers

import org.stepic.droid.model.DbVideoUrl
import org.stepik.android.model.structure.VideoUrl

fun VideoUrl.toDbUrl(videoId: Long) =
        DbVideoUrl(
                videoId = videoId,
                url = this.url,
                quality = this.quality
        )

fun DbVideoUrl.toVideoUrl() =
        VideoUrl(
                this.url,
                this.quality
        )

fun List<DbVideoUrl?>.toVideoUrls() = mapNotNull { it?.toVideoUrl() }