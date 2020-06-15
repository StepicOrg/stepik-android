package org.stepik.android.domain.step_content_video.interactor

import android.media.MediaMetadataRetriever
import io.reactivex.Maybe
import org.stepic.droid.persistence.model.StepPersistentWrapper
import ru.nobird.android.domain.rx.toMaybe
import org.stepik.android.domain.step_content_video.mapper.VideoLengthMapper
import org.stepik.android.model.Video
import javax.inject.Inject

class VideoLengthInteractor
@Inject
constructor(
    private val videoLengthMapper: VideoLengthMapper
) {
    /**
     * Returns length of video in [stepWrapper] formatted as string 00:00
     */
    fun getVideoLengthFormatted(stepWrapper: StepPersistentWrapper): Maybe<String> =
        getVideoLength(stepWrapper)
            .map(videoLengthMapper::mapVideoLengthFromMsToString)

    /**
     * Returns length of video in [stepWrapper] in ms
     */
    private fun getVideoLength(stepWrapper: StepPersistentWrapper): Maybe<Long> =
        (getVideoPath(stepWrapper.cachedVideo) ?: getVideoPath(stepWrapper.step.block?.video))
            .toMaybe()
            .map { videoPath ->
                val metadataRetriever = MediaMetadataRetriever()
                try {
                    metadataRetriever.setDataSource(videoPath) // local
                } catch (exception: IllegalArgumentException) {
                    metadataRetriever.setDataSource(videoPath, emptyMap()) // internet
                }

                metadataRetriever
                    .extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    .toLong()
            }
            .onErrorComplete()

    private fun getVideoPath(video: Video?): String? =
        video?.urls?.firstOrNull()?.url
}