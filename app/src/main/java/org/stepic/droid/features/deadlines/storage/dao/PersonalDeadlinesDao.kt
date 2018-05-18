package org.stepic.droid.features.deadlines.storage.dao

import org.stepic.droid.features.deadlines.model.DeadlineFlatItem
import org.stepic.droid.storage.dao.IDao
import java.util.*

interface PersonalDeadlinesDao: IDao<DeadlineFlatItem> {
    fun getClosestDeadlineDate(): Date?
    fun getDeadlinesForDate(date: Date, gap: Long): List<DeadlineFlatItem>
}