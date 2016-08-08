package org.stepic.droid.ui.presenters

interface Presenter<V> {

    fun attachView(view: V)

    fun detachView(view: V)
}
