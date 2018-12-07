package org.stepic.droid.notifications.handlers

import android.content.Context

interface RemoteMessageHandler {
    fun handleMessage(context: Context, rawMessage: String?)
}