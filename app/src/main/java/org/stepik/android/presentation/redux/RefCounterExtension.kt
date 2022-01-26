package org.stepik.android.presentation.redux

import ru.nobird.app.presentation.redux.feature.Feature

fun <State, Message, Action> Feature<State, Message, Action>.wrapWithRefCounter(): Feature<State, Message, Action> =
    object : Feature<State, Message, Action> by this {
        private var refCounter = 0
        override fun  addStateListener(listener: (state: State) -> Unit) {
            synchronized(this) {
                refCounter++
                this@wrapWithRefCounter.addStateListener(listener)
            }
        }

        override fun cancel() {
            synchronized(this) {
                refCounter--
                if (refCounter == 0) {
                    this@wrapWithRefCounter.cancel()
                }
            }
        }
    }