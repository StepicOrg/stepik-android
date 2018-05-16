package org.stepic.droid.web.storage.deadlines

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.stepic.droid.model.deadlines.DeadlinesWrapper
import org.stepic.droid.web.storage.model.StorageRecord

interface DeadlinesRepository {

    fun createDeadlinesForCourse(deadlines: DeadlinesWrapper): Single<StorageRecord<DeadlinesWrapper>>
    fun updateDeadlinesForCourse(record: StorageRecord<DeadlinesWrapper>): Single<StorageRecord<DeadlinesWrapper>>

    fun removeDeadlinesForCourse(recordId: Long): Completable
    fun getDeadlinesForCourse(courseId: Long): Single<StorageRecord<DeadlinesWrapper>>

    fun fetchAllDeadlines(): Observable<StorageRecord<DeadlinesWrapper>>

}