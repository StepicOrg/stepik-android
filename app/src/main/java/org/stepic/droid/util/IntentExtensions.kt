package org.stepic.droid.util

import android.content.Context
import android.content.Intent
import android.net.Uri

fun Intent.createEmailOnlyChooserIntent(context: Context, title: CharSequence): Intent {
    val intents = mutableListOf<Intent>()
    val dummyIntent = Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "info@domain.com", null))
    val activities = context.packageManager.queryIntentActivities(dummyIntent, 0)

    for (resolveInfo in activities) {
        val target = Intent(this)
        target.setPackage(resolveInfo.activityInfo.packageName)
        intents.add(target)
    }

    return if (!intents.isEmpty()) {
        val chooserIntent = Intent.createChooser(intents.removeAt(0), title)
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents.toTypedArray())
        chooserIntent
    } else {
        Intent.createChooser(this, title)
    }
}