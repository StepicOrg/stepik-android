package org.stepik.android.presentation.base

import android.arch.lifecycle.ViewModel
import android.os.Bundle
import android.support.annotation.CallSuper
import io.reactivex.disposables.CompositeDisposable
import org.stepic.droid.core.presenters.PresenterContract

abstract class PresenterBase<V> : PresenterContract<V>, ViewModel() {
    protected val compositeDisposable = CompositeDisposable()

    @Volatile
    var view: V? = null
        private set

    @CallSuper
    override fun attachView(view: V) {
        val previousView = this.view

        if (previousView != null) {
            throw IllegalStateException("Previous view is not detached! previousView = $previousView")
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

    @CallSuper
    override fun onCleared() {
        compositeDisposable.dispose()
    }

    open fun onSaveInstanceState(outState: Bundle) {}
    open fun onRestoreInstanceState(savedInstanceState: Bundle) {}
}
