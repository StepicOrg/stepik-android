package org.stepic.droid.base

import javax.inject.Inject


class ClientImpl<T>
@Inject
constructor(private val listenerContainerBase: ListenerContainer<T>) : Client<T> {

    override fun subscribe(listener: T) {
        listenerContainerBase.add(listener)
    }

    override fun unsubscribe(listener: T) {
        listenerContainerBase.remove(listener)
    }
}
