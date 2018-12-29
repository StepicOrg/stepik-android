package org.stepik.android.cache.personal_deadlines.dao

import org.stepik.android.cache.personal_deadlines.model.DeadlineEntity
import org.stepic.droid.storage.dao.IDao
import java.util.*

interface PersonalDeadlinesDao: IDao<DeadlineEntity> {
    fun getClosestDeadlineDate(): Date?
    fun getDeadlinesBetween(from: Date, to: Date): List<DeadlineEntity>
}