package org.stepik.android.presentation.base

import android.view.View
import ru.nobird.android.presentation.base.PresenterBase
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

abstract class PresenterViewHolder<V, P : PresenterBase<V>>(root: View) : DelegateViewHolder<P>(root) {
    private var presenter: P? = null

    override fun onBind(data: P) {
        presenter = data
        attachView(data)
    }

    override fun onUnbind() {
        presenter?.let(::detachView)
        presenter = null
    }

    protected abstract fun attachView(data: P)
    protected abstract fun detachView(data: P)
}