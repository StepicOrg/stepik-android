package org.stepic.droid.util

import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.model.CachedVideo
import org.stepik.android.model.structure.Video
import org.stepic.droid.preferences.UserPreferences
import org.stepic.droid.storage.operations.DatabaseFacade
import java.io.File
import javax.inject.Inject


class VideoFileResolver
@Inject
constructor(
        private val analytic: Analytic,
        private val databaseFacade: DatabaseFacade,
        private val userPreferences: UserPreferences,

        @BackgroundScheduler
        private val backgroundScheduler: Scheduler
) {
    private val compositeDisposable = CompositeDisposable()

    fun resolveVideoFile(video: Video?, stepId: Long): Video? {
        val cachedVideo = video?.transformToCachedVideo()
        cachedVideo?.stepId = stepId

        val cache = resolveVideoFile(cachedVideo).transformToVideo()
        video?.urls = cache?.urls
        return video
    }

    fun resolveVideoFile(cachedVideo: CachedVideo?): CachedVideo? {
        val path = cachedVideo?.url ?: return null

        try {
            if (File(path).exists()) {
                return cachedVideo
            }

            val fileNameFilter = { _: File, name: String ->
                name == cachedVideo.videoId.toString() + AppConstants.VIDEO_EXTENSION
            }

            val videoFile = (userPreferences.userDownloadFolder.listFiles(fileNameFilter) +
                    (userPreferences.sdCardDownloadFolder?.listFiles(fileNameFilter) ?: emptyArray())).firstOrNull()

            videoFile?.absolutePath?.let {
                cachedVideo.url = it

                // create a task to update video in DB
                compositeDisposable.add(
                        Completable.fromAction {
                            databaseFacade.addVideo(cachedVideo)
                        }.subscribeOn(backgroundScheduler).subscribe({
                            analytic.reportEvent(Analytic.Video.VIDEO_FILE_RESTORED)
                        }, {
                            analytic.reportError(Analytic.Error.CANT_RESTORE_VIDEO_FILE, it)
                        })
                )

                return cachedVideo
            }
        } catch (e: Exception) {
            analytic.reportError(Analytic.Error.CANT_RESTORE_VIDEO_FILE, e)
        }
        return null
    }

}