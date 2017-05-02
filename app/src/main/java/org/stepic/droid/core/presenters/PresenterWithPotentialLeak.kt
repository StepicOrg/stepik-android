package org.stepic.droid.core.presenters

import org.stepic.droid.analytic.Analytic

abstract class PresenterWithPotentialLeak<V>(protected val analytic: Analytic) : PresenterContract <V> {

    @Volatile
    var view: V? = null
        private set

    override fun attachView(view: V) {
        val previousView = this.view

        if (previousView != null) {
            val exception = IllegalStateException("Previous view is not detached! previousView = " + previousView)
            analytic.reportError(Analytic.Error.PREVIOUS_VIEW_NOT_DETACHED, exception)
        }

        this.view = view
    }

    override fun detachView(view: V) {
        val previousView = this.view

        if (previousView !== view) {
            val exception = IllegalStateException("Unexpected view! previousView = $previousView, getView to unbind = $view")
            analytic.reportError(Analytic.Error.UNEXPECTED_VIEW, exception)
        }
        this.view = null
    }

}
