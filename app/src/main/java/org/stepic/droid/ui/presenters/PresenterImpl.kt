package org.stepic.droid.ui.presenters

import android.support.annotation.CallSuper

abstract class PresenterImpl<V> : Presenter<V>{
    @Volatile
    public var view: V? = null
        private set

    @CallSuper
    override fun attachView(view: V) {
        val previousView = this.view

        if (previousView != null) {
            throw IllegalStateException("Previous getView is not detached! previousView = " + previousView)
        }

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
