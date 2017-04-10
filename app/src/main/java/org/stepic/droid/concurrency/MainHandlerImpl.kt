package org.stepic.droid.concurrency

import android.os.Handler
import org.stepic.droid.base.App
import javax.inject.Inject

class MainHandlerImpl @Inject constructor() : HandlerBaseDelegate(), MainHandler {
    val mainHandler = Handler(App.getAppContext().mainLooper)

    override fun getHandler() = mainHandler
}
