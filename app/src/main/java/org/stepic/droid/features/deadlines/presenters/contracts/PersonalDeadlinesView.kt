package org.stepic.droid.features.deadlines.presenters.contracts

import org.stepic.droid.features.deadlines.model.DeadlinesWrapper
import org.stepic.droid.web.storage.model.StorageRecord

interface PersonalDeadlinesView {
    sealed class State {
        object Idle: State()
        object Loading: State()
        object BlockingLoading: State()
        object Error: State()
        object EmptyDeadlines: State()
        class Deadlines(val record: StorageRecord<DeadlinesWrapper>): State()
    }

    fun setDeadlines(record: StorageRecord<DeadlinesWrapper>?)
    fun showLearningRateDialog()
    fun showLoadingDialog()
    fun showPersonalDeadlinesError()
}