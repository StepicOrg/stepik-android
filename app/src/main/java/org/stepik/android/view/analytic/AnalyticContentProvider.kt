package org.stepik.android.view.analytic

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import org.stepic.droid.di.storage.DaggerStorageComponent
import org.stepik.android.domain.analytic.repository.AnalyticRepository
import org.stepik.android.view.injection.analytic.AnalyticComponent
import org.stepik.android.view.injection.analytic.DaggerAnalyticComponent
import javax.inject.Inject

class AnalyticContentProvider : ContentProvider() {
    companion object {
        const val FLUSH = "flush"
    }

    private lateinit var component: AnalyticComponent

    @Inject
    internal lateinit var analyticRepository: AnalyticRepository

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

        component.inject(this)
    }

    override fun call(method: String, arg: String?, extras: Bundle?): Bundle? {
        if (::component.isInitialized) {
            injectComponent()
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
}