package org.stepic.droid.concurrency

import android.os.Handler
import org.stepic.droid.base.MainApplication

class MainHandlerImpl : HandlerBaseDelegate(), MainHandler {
    val mainHandler = Handler(MainApplication.getAppContext().mainLooper)

    override fun getHandler() = mainHandler
}
