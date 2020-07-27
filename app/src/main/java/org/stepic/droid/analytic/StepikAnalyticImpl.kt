package org.stepic.droid.analytic

import android.content.Context
import android.net.Uri
import androidx.core.os.bundleOf
import com.google.gson.JsonObject
import org.stepic.droid.BuildConfig
import org.stepic.droid.di.AppSingleton
import org.stepik.android.view.analytic.AnalyticContentProvider
import javax.inject.Inject

@AppSingleton
class StepikAnalyticImpl
@Inject
constructor(
    private val context: Context
) : StepikAnalytic {
    companion object {
        private const val ANALYTIC_URI = "content://${BuildConfig.APPLICATION_ID}.analytic_provider"
    }
    override fun flushEvents() {
        context.contentResolver.call(Uri.parse(ANALYTIC_URI), AnalyticContentProvider.FLUSH, null, null)
    }

    override fun logEvent(eventName: String, properties: Map<String, Any>) {
        context.contentResolver.call(Uri.parse(ANALYTIC_URI), AnalyticContentProvider.LOG, eventName, bundleOf(*properties.map { (a, b) -> a to b }.toTypedArray()))
    }
}