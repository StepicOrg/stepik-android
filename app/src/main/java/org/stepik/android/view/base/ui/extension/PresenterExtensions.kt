package org.stepik.android.view.base.ui.extension

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import org.stepik.android.presentation.base.ReduxPresenter
import ru.nobird.android.presentation.redux.container.ReduxView
import kotlin.reflect.KClass

inline fun <F, State, Message, Action, reified P : ReduxPresenter<State, Message, Action>> F.presenter(
    view: ReduxView<State, Action>,
    noinline factoryProducer: () -> ViewModelProvider.Factory
): Lazy<P> where F : LifecycleOwner, F : ViewModelStoreOwner  =
    PresenterLazy(lifecycle, view, P::class, viewModelStoreOwner = this, factoryProducer)

class PresenterLazy<State, Message, Action, P : ReduxPresenter<State, Message, Action>>(
    lifecycle: Lifecycle,
    view: ReduxView<State, Action>,
    private val viewModelClass: KClass<P>,
    private val viewModelStoreOwner: ViewModelStoreOwner,
    private val factoryProducer: () -> ViewModelProvider.Factory
) : Lazy<P> {
    init {
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                value.attachView(view)
            }

            override fun onStop(owner: LifecycleOwner) {
                value.detachView(view)
            }
        })
    }

    private var cached: P? = null

    override val value: P
        get() =
            cached ?: ViewModelProvider(viewModelStoreOwner.viewModelStore, factoryProducer())
                .get(viewModelClass.java)
                .also {
                    cached = it
                }

    override fun isInitialized(): Boolean =
        cached != null
}