package org.stepik.android.presentation.base

import android.view.View
import ru.nobird.android.presentation.base.PresenterBase
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

abstract class PresenterViewHolder<V>(root: View) : DelegateViewHolder<PresenterBase<V>>(root) {
    private var presenter: PresenterBase<V>? = null

    override fun onBind(data: PresenterBase<V>) {
        presenter = data
        attachView(data)
    }

    override fun onUnbind() {
        presenter?.let(::detachView)
        presenter = null
    }

    protected abstract fun attachView(data: PresenterBase<V>)
    protected abstract fun detachView(data: PresenterBase<V>)
}