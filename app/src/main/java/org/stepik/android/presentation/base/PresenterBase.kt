package org.stepik.android.presentation.base

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import org.stepic.droid.core.presenters.PresenterContract
import org.stepik.android.presentation.profile_courses.ViewContainer

abstract class PresenterBase<V>(
    private val viewContainer: ViewContainer<V> = object : ViewContainer<V> { override var view: V? = null }
) : PresenterContract<V>, ViewModel() {
    protected val compositeDisposable = CompositeDisposable()

    var view: V? by viewContainer::view
//        private set(value) {
//            viewContainer.view = value
//        }
//        get() = viewContainer.view

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

    @CallSuper
    override fun onCleared() {
        compositeDisposable.dispose()
    }

    open fun onSaveInstanceState(outState: Bundle) {}
    open fun onRestoreInstanceState(savedInstanceState: Bundle) {}
}
