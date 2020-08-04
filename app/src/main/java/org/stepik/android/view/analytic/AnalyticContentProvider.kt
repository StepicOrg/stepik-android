package org.stepik.android.view.analytic

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.PublishSubject
import org.stepic.droid.base.App
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepik.android.domain.analytic.interactor.AnalyticInteractor
import org.stepik.android.view.injection.analytic.AnalyticComponent
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AnalyticContentProvider : ContentProvider() {
    companion object {
        private const val FLUSH_INTERVAL = 30L

        const val FLUSH = "flush"
        const val LOG = "log"
    }

    private lateinit var component: AnalyticComponent

    private val compositeDisposable = CompositeDisposable()

    private val analyticsSubject = PublishSubject.create<Unit>()

    @Inject
    @BackgroundScheduler
    lateinit var backgroundScheduler: Scheduler

    @Inject
    lateinit var analyticInteractor: AnalyticInteractor

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        throw UnsupportedOperationException()
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        throw UnsupportedOperationException()
    }

    override fun onCreate(): Boolean =
        true

    private fun injectComponent() {
        if (context == null) return
        component = App.component()
            .analyticProviderComponentBuilder()
            .build()
        component.inject(this)

        subscribeForFlushUpdates()
    }

    private fun subscribeForFlushUpdates() {
        val timerSource = Observable
            .interval(FLUSH_INTERVAL, TimeUnit.SECONDS)
            .map { Unit }

        compositeDisposable += Observable
            .merge(timerSource, analyticsSubject)
            .concatMapCompletable {
                analyticInteractor.flushEvents()
            }
            .subscribeOn(backgroundScheduler)
            .subscribeBy(
                onError = { subscribeForFlushUpdates() }
            )
    }

    override fun call(method: String, arg: String?, extras: Bundle?): Bundle? {
        if (!::component.isInitialized) {
            injectComponent()
        }
        when (method) {
            LOG ->
                logEvent(arg, extras)

            FLUSH ->
                flushEvents()
        }
        return super.call(method, arg, extras)
    }

    private fun logEvent(eventName: String?, bundle: Bundle?) {
        if (eventName == null || bundle == null) return
        compositeDisposable += analyticInteractor
            .logEvent(eventName, bundle)
            .subscribeOn(backgroundScheduler)
            .subscribe()
    }

    private fun flushEvents() {
        analyticsSubject.onNext(Unit)
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        throw UnsupportedOperationException()
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        throw UnsupportedOperationException()
    }

    override fun getType(uri: Uri): String? {
        throw UnsupportedOperationException()
    }
}