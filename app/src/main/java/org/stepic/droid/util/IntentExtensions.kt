package org.stepic.droid.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.util.*

fun Intent.createEmailOnlyChooserIntent(context: Context, title: CharSequence): Intent {
    val intents = Stack<Intent>()
    val dummyIntent = Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "info@domain.com", null))
    val activities = context.packageManager.queryIntentActivities(dummyIntent, 0)

    for (resolveInfo in activities) {
        val target = Intent(this)
        target.setPackage(resolveInfo.activityInfo.packageName)
        intents.add(target)
    }

    return if (!intents.empty()) {
        val chooserIntent = Intent.createChooser(intents.removeAt(0), title)
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents.toArray())
        chooserIntent
    } else {
        Intent.createChooser(this, title)
    }
}