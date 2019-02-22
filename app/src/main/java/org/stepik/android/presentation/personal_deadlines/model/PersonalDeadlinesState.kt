package org.stepik.android.presentation.personal_deadlines.model

import org.stepic.droid.web.storage.model.StorageRecord
import org.stepik.android.domain.personal_deadlines.model.DeadlinesWrapper

sealed class PersonalDeadlinesState {
    object Idle : PersonalDeadlinesState()
    object EmptyDeadlines : PersonalDeadlinesState()
    object NoDeadlinesNeeded : PersonalDeadlinesState()
    class Deadlines(val record: StorageRecord<DeadlinesWrapper>) : PersonalDeadlinesState()
}