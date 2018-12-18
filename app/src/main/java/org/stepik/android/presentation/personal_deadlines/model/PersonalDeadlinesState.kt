package org.stepik.android.presentation.personal_deadlines.model

import org.stepik.android.domain.personal_deadlines.model.DeadlinesWrapper
import org.stepic.droid.web.storage.model.StorageRecord

sealed class PersonalDeadlinesState {
    object Idle: PersonalDeadlinesState()
    object EmptyDeadlines: PersonalDeadlinesState()
    object NoDeadlinesNeeded: PersonalDeadlinesState()
    class Deadlines(val record: StorageRecord<DeadlinesWrapper>): PersonalDeadlinesState()
}