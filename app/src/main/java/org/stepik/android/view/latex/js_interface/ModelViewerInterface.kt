package org.stepik.android.view.latex.js_interface

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.JavascriptInterface
import android.widget.Toast
import org.stepic.droid.R

class ModelViewerInterface(private val context: Context) {
    companion object {
        const val MODEL_VIEWER_INTERFACE = "ModelViewerInterface"
    }
    @JavascriptInterface
    fun handleARModel(url: String) {
        val sceneViewerIntent = Intent(Intent.ACTION_VIEW)
        sceneViewerIntent.data = Uri.parse(url)
        sceneViewerIntent.setPackage("com.google.android.googlequicksearchbox")
        sceneViewerIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(sceneViewerIntent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, R.string.ar_not_supported_alert, Toast.LENGTH_SHORT).show()
        }
    }
}