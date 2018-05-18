package org.stepic.droid.features.deadlines.repository

import com.google.gson.GsonBuilder
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.toObservable
import org.stepic.droid.jsonHelpers.adapters.UTCDateAdapter
import org.stepic.droid.model.Course
import org.stepic.droid.web.storage.model.StorageRecord
import org.stepic.droid.features.deadlines.model.DeadlinesWrapper
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.web.CoursesMetaResponse
import org.stepic.droid.web.StepicRestLoggedService
import org.stepic.droid.web.storage.RemoteStorageService
import org.stepic.droid.web.storage.model.StorageRecordWrapped
import org.stepic.droid.web.storage.model.StorageRequest
import org.stepic.droid.web.storage.model.StorageResponse
import java.util.*

// todo 14.05.2018: inject this class after api refactor
class DeadlinesRepositoryImpl(
        private val loggedService: StepicRestLoggedService,
        private val remoteStorageService: RemoteStorageService,
        private val sharedPreferenceHelper: SharedPreferenceHelper
): DeadlinesRepository {
    private val gson = GsonBuilder()
            .registerTypeAdapter(Date::class.java, UTCDateAdapter())
            .create()

    private fun Single<StorageResponse>.unwrap() =
            map { it.records.first().unwrap<DeadlinesWrapper>(gson) }

    override fun createDeadlinesForCourse(deadlines: DeadlinesWrapper): Single<StorageRecord<DeadlinesWrapper>> =
            remoteStorageService.createStorageRecord(createStorageRequest(deadlines)).unwrap()

    override fun updateDeadlinesForCourse(record: StorageRecord<DeadlinesWrapper>): Single<StorageRecord<DeadlinesWrapper>> =
                remoteStorageService.setStorageRecord(record.id ?: 0, StorageRequest(record.wrap(gson))).unwrap()

    override fun removeDeadlinesForCourse(recordId: Long): Completable = remoteStorageService.removeStorageRecord(recordId)

    override fun getDeadlinesForCourse(courseId: Long): Maybe<StorageRecord<DeadlinesWrapper>> =
            remoteStorageService.getStorageRecords(1, sharedPreferenceHelper.profile?.id ?: -1, getKind(courseId)).singleOrError()
                    .flatMapMaybe {
                        if (it.records.isNotEmpty()) {
                            Maybe.just(it.records.first().unwrap<DeadlinesWrapper>(gson))
                        } else {
                            Maybe.empty()
                        }
                    }

    override fun fetchAllDeadlines(): Observable<StorageRecord<DeadlinesWrapper>> =
            getAllEnrolledCourses().flatMap {
                getDeadlinesForCourse(it.courseId).toObservable()
            }

    private fun getEnrolledCourses(page: Int): Observable<CoursesMetaResponse> =
            loggedService.getEnrolledCourses(page).toObservable()

    private fun getAllEnrolledCourses(): Observable<Course> =
            getEnrolledCourses(1).concatMap {
                if (it.meta.has_next) {
                    Observable.just(it).concatWith(getEnrolledCourses(it.meta.page + 1))
                } else {
                    Observable.just(it)
                }
            }.concatMap {
                it.courses.toObservable()
            }

    private fun getKind(courseId: Long) = "deadline_$courseId"

    private fun createStorageRequest(deadlines: DeadlinesWrapper, recordId: Long? = null) =
            StorageRequest(StorageRecordWrapped(recordId, kind = getKind(deadlines.course), data = gson.toJsonTree(deadlines)))
}