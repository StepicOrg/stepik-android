package org.stepik.android.view.analytic

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import io.reactivex.Scheduler
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.storage.DaggerStorageComponent
import org.stepic.droid.util.DebugToolsHelper
import org.stepik.android.domain.analytic.interactor.AnalyticInteractor
import org.stepik.android.view.injection.analytic.AnalyticComponent
import org.stepik.android.view.injection.analytic.DaggerAnalyticComponent
import timber.log.Timber
import javax.inject.Inject

class AnalyticContentProvider : ContentProvider() {
    companion object {
        const val FLUSH = "flush"
        const val LOG = "log"
    }

    private lateinit var component: AnalyticComponent

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
        component = DaggerAnalyticComponent.builder()
            .context(context)
            .setStorageComponent(DaggerStorageComponent
                .builder()
                .context(context)
                .build())
            .build()

        Timber.d("Injecting")
        component.inject(this)
    }

    override fun call(method: String, arg: String?, extras: Bundle?): Bundle? {
        if (!::component.isInitialized) {
            DebugToolsHelper.initDebugTools(context)
            injectComponent()
        }
        when (method) {
            LOG -> {
                Timber.d("Log")
                logEvent(arg, extras)
            }
            FLUSH -> {
                Timber.d("Flush")
                flushEvents()
            }
        }
        return super.call(method, arg, extras)
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

    private fun logEvent(eventName: String?, bundle: Bundle?) {
        if (eventName == null || bundle == null) return
        analyticInteractor
            .logEvent(eventName, bundle)
            .subscribeOn(backgroundScheduler)
            .subscribe()
    }

    private fun flushEvents() {
        analyticInteractor
            .flushEvents()
            .subscribeOn(backgroundScheduler)
            .subscribe()
    }
}