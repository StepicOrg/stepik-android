package org.stepic.droid.storage.dao

import org.stepic.droid.adaptive.model.LocalExpItem

interface AdaptiveExpDao : IDao<LocalExpItem> {

    fun getExpItem(courseId: Long, submissionId: Long = -1): LocalExpItem?

    fun getExpForCourse(courseId: Long): Long

}