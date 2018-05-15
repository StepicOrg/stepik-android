package org.stepic.droid.web.storage.deadlines

import com.google.gson.GsonBuilder
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.toObservable
import org.stepic.droid.jsonHelpers.adapters.UTCDateAdapter
import org.stepic.droid.model.Course
import org.stepic.droid.model.StorageRecord
import org.stepic.droid.model.deadlines.DeadlinesWrapper
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.util.toObject
import org.stepic.droid.web.CoursesMetaResponse
import org.stepic.droid.web.StepicRestLoggedService
import org.stepic.droid.web.storage.RemoteStorageService
import org.stepic.droid.web.storage.model.StorageRequest
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

    override fun createDeadlinesForCourse(deadlines: DeadlinesWrapper): Completable =
            remoteStorageService.createStorageRecord(StorageRequest(createStorageRecord(deadlines)))

    override fun updateDeadlinesForCourse(deadlines: DeadlinesWrapper): Completable =
            getStorageRecordForCourse(deadlines.course).flatMapCompletable {
                val recordId = it.id!! // we should fail if there no such record
                remoteStorageService.setStorageRecord(recordId, StorageRequest(createStorageRecord(deadlines, recordId)))
            }

    override fun removeDeadlinesForCourse(courseId: Long): Completable =
            getStorageRecordForCourse(courseId).flatMapCompletable {
                remoteStorageService.removeStorageRecord(it.id!!)
            }

    override fun getDeadlinesForCourse(courseId: Long): Single<DeadlinesWrapper> =
            getStorageRecordForCourse(courseId).map { it.data.toObject<DeadlinesWrapper>(gson) }

    override fun fetchAllDeadlines(): Observable<DeadlinesWrapper> =
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

    private fun createStorageRecord(deadlines: DeadlinesWrapper, recordId: Long? = null) =
            StorageRecord(recordId, kind = getKind(deadlines.course), data = gson.toJsonTree(deadlines))

    private fun getStorageRecordForCourse(courseId: Long): Single<StorageRecord> =
            remoteStorageService.getStorageRecords(1, sharedPreferenceHelper.profile?.id ?: -1, getKind(courseId))
                    .map { it.records.first() }
                    .singleOrError()

}