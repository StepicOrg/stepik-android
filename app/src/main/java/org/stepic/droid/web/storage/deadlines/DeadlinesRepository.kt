package org.stepic.droid.web.storage.deadlines

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.stepic.droid.model.deadlines.DeadlinesWrapper

interface DeadlinesRepository {

    fun createDeadlinesForCourse(deadlines: DeadlinesWrapper): Completable
    fun updateDeadlinesForCourse(deadlines: DeadlinesWrapper): Completable

    fun removeDeadlinesForCourse(courseId: Long): Completable
    fun getDeadlinesForCourse(courseId: Long): Single<DeadlinesWrapper>

    fun fetchAllDeadlines(): Observable<DeadlinesWrapper>

}