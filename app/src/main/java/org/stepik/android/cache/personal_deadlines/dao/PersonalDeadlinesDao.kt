package org.stepik.android.cache.personal_deadlines.dao

import org.stepic.droid.storage.dao.IDao
import org.stepik.android.cache.personal_deadlines.model.DeadlineEntity
import java.util.Date

interface PersonalDeadlinesDao : IDao<DeadlineEntity> {
    fun getClosestDeadlineDate(): Date?
    fun getDeadlinesBetween(from: Date, to: Date): List<DeadlineEntity>
}