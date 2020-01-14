package org.stepic.droid.persistence.downloads

import android.app.DownloadManager
import android.content.Context
import android.widget.Toast
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.PublishSubject
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.persistence.di.PersistenceScope
import org.stepic.droid.persistence.model.SystemDownloadRecord
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@PersistenceScope
class DownloadErrorPoster
@Inject
constructor(
    private val analytic: Analytic,
    private val context: Context,

    @BackgroundScheduler
    private val scheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) {
    companion object {
        private const val ERROR_DEBOUNCE_MS = 500L
    }

    private val errorsSubject =
        PublishSubject.create<DownloadError>()

    private val compositeDisposable = CompositeDisposable()

    init {
        initReporter()
    }

    private fun initReporter() {
        compositeDisposable.clear()
        compositeDisposable += errorsSubject
            .doOnNext {
                when (it) {
                    is DownloadError.Record ->
                        analytic
                            .reportEventWithName(
                                Analytic.DownloaderV2.SYSTEM_DOWNLOAD_ERROR,
                                "title = ${it.record.title}, localUri = ${it.record.localUri}, reason = ${it.record.reason}"
                            )

                    is DownloadError.DownloadManager ->
                        analytic
                            .reportError(
                                Analytic.DownloaderV2.SYSTEM_DOWNLOAD_ERROR,
                                it.cause
                            )
                }
            }
            .debounce(ERROR_DEBOUNCE_MS, TimeUnit.MILLISECONDS)
            .map(::resolveErrorMessage)
            .observeOn(mainScheduler)
            .subscribeOn(scheduler)
            .subscribeBy(onError = { initReporter() }, onNext = ::showError)
    }

    private fun resolveErrorMessage(downloadError: DownloadError): String =
        when (downloadError) {
            is DownloadError.Record ->
                context.getString(R.string.download_error, downloadError.record.title + resolveErrorDescription(downloadError.record))

            is DownloadError.DownloadManager ->
                context.getString(R.string.download_error_system_manager)
        }


    private fun resolveErrorDescription(record: SystemDownloadRecord): String =
        when(record.reason) {
            DownloadManager.ERROR_INSUFFICIENT_SPACE ->
                context.getString(R.string.download_error_insufficient_storage)
            else ->
                ""
        }

    private fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun onRecordError(systemDownloadRecord: SystemDownloadRecord) {
        errorsSubject.onNext(DownloadError.Record(systemDownloadRecord))
    }

    fun onDownloadManagerError(cause: Throwable) {
        errorsSubject.onNext(DownloadError.DownloadManager(cause))
    }

    private sealed class DownloadError {
        data class Record(val record: SystemDownloadRecord) : DownloadError()
        data class DownloadManager(val cause: Throwable) : DownloadError()
    }
}