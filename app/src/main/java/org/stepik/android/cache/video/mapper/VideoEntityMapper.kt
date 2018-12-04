package org.stepik.android.cache.video.mapper

import org.stepik.android.cache.video.model.VideoEntity
import org.stepik.android.cache.video.model.VideoUrlEntity
import org.stepik.android.model.Video
import org.stepik.android.model.VideoUrl
import javax.inject.Inject

class VideoEntityMapper
@Inject
constructor() {

    fun entityToVideo(videoEntity: VideoEntity, videoUrlEntities: List<VideoUrlEntity>) : Video =
        Video(
            id = videoEntity.id,
            thumbnail = videoEntity.thumbnail,
            duration = videoEntity.duration,
            urls = videoUrlEntities.map { VideoUrl(it.url, it.quality) }
        )

    fun videoToEntity(video: Video): Pair<VideoEntity, List<VideoUrlEntity>> =
        VideoEntity(
            id = video.id,
            thumbnail = video.thumbnail,
            duration = video.duration
        ) to (video
            .urls
            ?.mapNotNull {
                val url = it.url ?: return@mapNotNull null
                val quality = it.quality ?: return@mapNotNull null

                VideoUrlEntity(video.id, url, quality)
            }
            ?: emptyList())

}