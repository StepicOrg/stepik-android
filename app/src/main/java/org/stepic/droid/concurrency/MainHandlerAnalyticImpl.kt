package org.stepic.droid.concurrency

import android.content.Context
import android.os.Handler
import javax.inject.Inject

class MainHandlerAnalyticImpl
@Inject
constructor(context: Context) : HandlerBaseDelegate(), MainHandler {
    val mainHandler = Handler(context.mainLooper)

    override fun getHandler(): Handler = mainHandler
}