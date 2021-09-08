package org.stepik.android.presentation.redux

import ru.nobird.android.presentation.redux.feature.Feature
import java.util.concurrent.atomic.AtomicInteger

fun <State, Message, Action> Feature<State, Message, Action>.wrapWithRefCounter(): Feature<State, Message, Action> =
    object : Feature<State, Message, Action> by this {
        private val refCounter = AtomicInteger(0)
        override fun addStateListener(listener: (state: State) -> Unit) {
            var isSuccessful = false
            while (!isSuccessful) {
                val refCounterValue = refCounter.get()
                val newValue = refCounterValue + 1
                isSuccessful = refCounter.compareAndSet(refCounterValue, newValue)
            }
            this@wrapWithRefCounter.addStateListener(listener)
        }

        override fun cancel() {
            var isSuccessful = false
            while (!isSuccessful) {
                val refCounterValue = refCounter.get()
                val newValue = refCounterValue - 1
                isSuccessful = refCounter.compareAndSet(refCounterValue, newValue)
            }
            if (refCounter.get() == 0) {
                this@wrapWithRefCounter.cancel()
            }
        }
    }