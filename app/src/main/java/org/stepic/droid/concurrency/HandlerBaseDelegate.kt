package org.stepic.droid.concurrency

import android.os.Handler

abstract class HandlerBaseDelegate : IHandler {
    override fun post(body: () -> Unit) = getHandler().post { body.invoke() }
    abstract fun getHandler() : Handler
}