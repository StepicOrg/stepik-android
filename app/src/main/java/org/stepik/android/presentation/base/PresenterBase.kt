package org.stepik.android.presentation.base

import android.arch.lifecycle.ViewModel
import android.support.annotation.CallSuper
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import org.stepic.droid.core.presenters.PresenterContract

abstract class PresenterBase<V> : PresenterContract<V>, ViewModel() {
    private val compositeDisposable = CompositeDisposable()

    @Volatile
    var view: V? = null
        private set

    @CallSuper
    override fun attachView(view: V) {
        val previousView = this.view

        if (previousView != null) {
            throw IllegalStateException("Previous view is not detached! previousView = " + previousView)
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

    protected fun addDisposable(disposable: Disposable) {
        compositeDisposable.add(disposable)
    }

    @CallSuper
    override fun onCleared() {
        compositeDisposable.dispose()
    }
}
