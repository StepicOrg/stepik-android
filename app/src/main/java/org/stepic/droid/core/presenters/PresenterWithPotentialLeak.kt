package org.stepic.droid.core.presenters

abstract class PresenterWithPotentialLeak<V> : PresenterContract <V> {

    @Volatile
    var view: V? = null
        private set

    override fun attachView(view: V) {
        this.view = view
    }

    override fun detachView(view: V) {
        this.view = null
    }

}
