package org.stepic.droid.concurrency

interface IHandler {
    fun post(body : ()->Unit): Boolean
}
