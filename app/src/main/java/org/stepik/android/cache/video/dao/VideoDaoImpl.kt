package org.stepik.android.cache.video.dao

import org.stepic.droid.storage.dao.IDao
import org.stepik.android.cache.video.mapper.VideoEntityMapper
import org.stepik.android.cache.video.model.VideoEntity
import org.stepik.android.cache.video.model.VideoUrlEntity
import org.stepik.android.cache.video.structure.VideoDbScheme
import org.stepik.android.cache.video.structure.VideoUrlDbScheme
import org.stepik.android.model.Video
import javax.inject.Inject

class VideoDaoImpl
@Inject
constructor(
    private val videoEntityMapper: VideoEntityMapper,
    private val videoEntityDao: IDao<VideoEntity>,
    private val videoUrlEntityDao: IDao<VideoUrlEntity>
) : VideoDao {
    override fun get(videoId: Long): Video? {
        val videoEntity = videoEntityDao
            .get(mapOf(VideoDbScheme.Columns.ID to videoId.toString()))
            ?: return null

        val videoUrlEntities = videoUrlEntityDao
            .getAll(mapOf(VideoUrlDbScheme.Columns.VIDEO_ID to videoId.toString()))

        return videoEntityMapper
            .entityToVideo(videoEntity, videoUrlEntities)
            .takeUnless { it.urls.isNullOrEmpty() }
    }

    override fun replace(video: Video) {
        val (videoEntity, videoUrlEntities) =
                videoEntityMapper.videoToEntity(video)

        videoUrlEntityDao.remove(mapOf(VideoUrlDbScheme.Columns.VIDEO_ID to videoEntity.id.toString()))
        videoEntityDao.insertOrReplace(videoEntity)
        videoUrlEntityDao.insertOrReplaceAll(videoUrlEntities)
    }

    override fun remove(videoId: Long) {
        videoUrlEntityDao.remove(mapOf(VideoUrlDbScheme.Columns.VIDEO_ID to videoId.toString()))
        videoEntityDao.remove(mapOf(VideoUrlDbScheme.Columns.VIDEO_ID to videoId.toString()))
    }
}