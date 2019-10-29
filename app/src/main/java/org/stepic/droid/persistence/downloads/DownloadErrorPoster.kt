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
        PublishSubject.create<SystemDownloadRecord>()

    private val compositeDisposable = CompositeDisposable()

    init {
        initReporter()
    }

    private fun initReporter() {
        compositeDisposable.clear()
        compositeDisposable += errorsSubject
            .doOnNext {
                analytic
                    .reportEventWithName(
                        Analytic.DownloaderV2.SYSTEM_DOWNLOAD_ERROR,
                        "title = ${it.title}, localUri = ${it.localUri}, reason = ${it.reason}"
                    )
            }
            .debounce(ERROR_DEBOUNCE_MS, TimeUnit.MILLISECONDS)
            .map(::resolveErrorMessage)
            .observeOn(mainScheduler)
            .subscribeOn(scheduler)
            .subscribeBy(onError = { initReporter() }, onNext = ::showError)
    }

    private fun resolveErrorMessage(record: SystemDownloadRecord): String =
        context.getString(R.string.download_error, record.title + resolveErrorDescription(record))

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

    fun onError(systemDownloadRecord: SystemDownloadRecord) {
        errorsSubject.onNext(systemDownloadRecord)
    }
}