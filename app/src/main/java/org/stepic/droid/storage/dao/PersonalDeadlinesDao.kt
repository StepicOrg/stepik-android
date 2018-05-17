package org.stepic.droid.storage.dao

import org.stepic.droid.model.deadlines.DeadlineFlatItem
import java.util.*

interface PersonalDeadlinesDao: IDao<DeadlineFlatItem> {
    fun getClosestDeadlineDate(): Date?
    fun getDeadlinesForDate(date: Date, gap: Long): List<DeadlineFlatItem>
}