package org.stepic.droid.core.presenters

import androidx.annotation.CallSuper

abstract class PresenterBase<V> : PresenterContract<V> {
    @Volatile
    var view: V? = null
        private set

    @CallSuper
    override fun attachView(view: V) {
        val previousView = this.view

        check(previousView == null) { "Previous view is not detached! previousView = $previousView" }

        this.view = view
    }

    @CallSuper
    override fun detachView(view: V) {
        val previousView = this.view

        if (previousView === view) {
            this.view = null
        } else {
            throw IllegalStateException("Unexpected view! previousView = $previousView, getView to unbind = $view")
        }
    }

}
