package org.stepic.droid.core.presenters.contracts

import org.stepic.droid.model.deadlines.DeadlinesWrapper
import org.stepic.droid.web.storage.model.StorageRecord

interface PersonalDeadlinesView {
    sealed class State {
        object Idle: State()
        object Loading: State()
        object Error: State()
        object EmptyDeadlines: State()
        class Deadlines(val record: StorageRecord<DeadlinesWrapper>): State()
    }

}