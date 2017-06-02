package org.stepic.droid.mappers

import org.stepic.droid.model.DbVideoUrl
import org.stepic.droid.model.VideoUrl

object VideoUrlMapper {
    fun VideoUrl.toDbUrl(videoId: Long): DbVideoUrl =
            DbVideoUrl(
                    videoId = videoId,
                    url = this.url,
                    quality = this.quality
            )
}
