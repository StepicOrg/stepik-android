package org.stepic.droid.concurrency

import android.os.Handler
import org.stepic.droid.base.App

class MainHandlerImpl : HandlerBaseDelegate(), MainHandler {
    val mainHandler = Handler(App.getAppContext().mainLooper)

    override fun getHandler() = mainHandler
}
