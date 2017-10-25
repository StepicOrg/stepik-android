package org.stepic.droid.core.downloadingProgress

import android.app.DownloadManager
import android.database.Cursor
import io.reactivex.Flowable
import io.reactivex.functions.BiFunction
import org.stepic.droid.model.DownloadEntity
import org.stepic.droid.model.Step
import org.stepic.droid.storage.CancelSniffer
import org.stepic.droid.storage.operations.DatabaseFacade
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
    }

    fun subscribe(stepIds: Set<Long>): Flowable<Float> {
        val stepNumber = stepIds.size
        return Flowable
                .combineLatest(steps(stepIds), downloadEntities(stepIds), BiFunction<List<Step>, List<DownloadEntity>, Float> { steps, downloadEntities ->
                    val downloadEntitiesHashMap = downloadEntities.associateBy { it.stepId }

                    val totalProgressCached: Float = steps
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


                    val progressPart = (totalProgressDownloading + totalProgressCached) / stepNumber.toFloat()
                    Timber.d("progress = $progressPart")
                    progressPart
                })
                .repeatWhen { completed -> completed.delay(POLISHING_DELAY, TimeUnit.MILLISECONDS) }
                .distinctUntilChanged()
    }


    private fun steps(stepIds: Set<Long>): Flowable<List<Step>> =
            Flowable
                    .fromCallable {
                        databaseFacade.getStepsById(stepIds = stepIds.toList())
                    }


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
}
