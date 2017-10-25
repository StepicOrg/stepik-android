package org.stepic.droid.core.downloadingProgress

import android.app.DownloadManager
import android.database.Cursor
import io.reactivex.Flowable
import io.reactivex.functions.BiFunction
import org.stepic.droid.model.DownloadEntity
import org.stepic.droid.model.Step
import org.stepic.droid.storage.CancelSniffer
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.RetryWithDelay
import org.stepic.droid.util.getInt
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class StepProgressPublisher
@Inject
constructor(
        private val databaseFacade: DatabaseFacade,
        private val systemDownloadManager: DownloadManager,
        private val cancelSniffer: CancelSniffer) {

    companion object {
        private const val POLISHING_DELAY = 300L
        private const val RETRY_DELAY = 300
    }

    fun subscribe(stepIds: Set<Long>): Flowable<Float> {
        return Flowable
                .combineLatest(videoSteps(stepIds), downloadEntities(stepIds), BiFunction<List<Step>, List<DownloadEntity>, Float> { videoSteps, downloadEntities ->
                    val downloadEntitiesHashMap = downloadEntities.associateBy { it.stepId }

                    val totalProgressCached: Float = videoSteps
                            .filterNot { downloadEntitiesHashMap.contains(it.id) }
                            .filter { it.is_cached }
                            .size
                            .toFloat()

                    val totalProgressDownloading = downloadEntities
                            .filterNot { cancelSniffer.isStepIdCanceled(it.stepId) } // canceled is 0f in totalProgressDownloading
                            .map { it.downloadId }
                            .let {
                                if (it.isEmpty()) {
                                    null
                                } else {
                                    val query = DownloadManager.Query()
                                    query.setFilterById(*it.toLongArray())
                                    systemDownloadManager.query(query)
                                }
                            }
                            .totalProgressOfDownloading()


                    val progressPart = (totalProgressDownloading + totalProgressCached) / videoSteps.size.toFloat()
                    Timber.d("progress = $progressPart")
                    progressPart
                })
                .repeatWhen { completed -> completed.delay(POLISHING_DELAY, TimeUnit.MILLISECONDS) }
                .distinctUntilChanged()
    }


    private fun videoSteps(stepIds: Set<Long>): Flowable<List<Step>> =
            Flowable
                    .fromCallable {
                        val steps = databaseFacade.getStepsById(stepIds = stepIds.toList())
                        if (steps.size != stepIds.size) {
                            throw StepsAreNotCached()
                        }
                        steps.filter { it.block?.name == AppConstants.TYPE_VIDEO }
                    }
                    .retryWhen(RetryWithDelay(RETRY_DELAY)) //wait until all steps will be in database


    private fun downloadEntities(stepIds: Set<Long>): Flowable<List<DownloadEntity>> =
            Flowable
                    .fromCallable {
                        databaseFacade.getDownloadEntitiesBy(stepIds.toLongArray())
                    }

    private fun Cursor?.totalProgressOfDownloading(): Float {
        var result = 0f
        if (this == null) {
            return result
        }
        this.use {
            it.moveToFirst()
            while (!it.isAfterLast) {
                val totalBytes = it.getInt(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                val downloadedBytes = it.getInt(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                val columnStatus = it.getInt(DownloadManager.COLUMN_STATUS)

                result +=
                        if (columnStatus == DownloadManager.STATUS_SUCCESSFUL) {
                            1f
                        } else {
                            downloadedBytes.toFloat() / totalBytes
                        }
                it.moveToNext()
            }

        }
        return result
    }

    private class StepsAreNotCached : Exception("wait, when all steps will in database")
}
