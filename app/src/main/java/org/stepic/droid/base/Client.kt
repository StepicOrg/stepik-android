package org.stepic.droid.base

interface Client<T> {

    fun subscribe(listener: T)

    fun unsubscribe(listener: T)
}
