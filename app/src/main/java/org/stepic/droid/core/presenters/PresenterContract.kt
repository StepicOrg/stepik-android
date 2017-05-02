package org.stepic.droid.core.presenters

interface PresenterContract<V> {
    fun attachView(view: V)
    fun detachView(view: V)
}
