package org.stepic.droid.features.deadlines.repository

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import org.stepic.droid.features.deadlines.model.DeadlinesWrapper
import org.stepik.android.model.structure.Course
import org.stepic.droid.web.storage.model.StorageRecord

interface DeadlinesRepository {

    fun createDeadlinesForCourse(deadlines: DeadlinesWrapper): Single<StorageRecord<DeadlinesWrapper>>
    fun updateDeadlinesForCourse(record: StorageRecord<DeadlinesWrapper>): Single<StorageRecord<DeadlinesWrapper>>

    fun removeDeadlinesForCourseByRecordId(recordId: Long): Completable
    fun removeDeadlinesForCourse(courseId: Long): Completable
    fun getDeadlinesForCourse(courseId: Long): Maybe<StorageRecord<DeadlinesWrapper>>

    fun syncDeadlines(enrolledCourses: List<Course>? = null): Completable

    fun shouldShowDeadlinesBannerForCourse(courseId: Long): Single<Boolean>
    fun hideDeadlinesBannerForCourse(courseId: Long): Completable

}