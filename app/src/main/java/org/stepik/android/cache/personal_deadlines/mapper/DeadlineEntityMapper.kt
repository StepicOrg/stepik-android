package org.stepik.android.cache.personal_deadlines.mapper

import org.stepik.android.domain.personal_deadlines.model.DeadlinesWrapper
import org.stepic.droid.web.storage.model.StorageRecord
import org.stepik.android.cache.personal_deadlines.model.DeadlineEntity
import javax.inject.Inject

class DeadlineEntityMapper
@Inject
constructor() {

    fun mapToEntity(record: StorageRecord<DeadlinesWrapper>): List<DeadlineEntity> =
        record
            .data
            .deadlines
            .map { deadline ->
                DeadlineEntity(record.id ?: -1, record.data.course, deadline.section, deadline.deadline)
            }

}