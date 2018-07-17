package org.stepic.droid.features.deadlines.repository

import com.google.gson.GsonBuilder
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.toObservable
import org.stepic.droid.di.AppSingleton
import org.stepic.droid.jsonHelpers.adapters.UTCDateAdapter
import org.stepik.android.model.structure.Course
import org.stepic.droid.web.storage.model.StorageRecord
import org.stepic.droid.features.deadlines.model.DeadlinesWrapper
import org.stepic.droid.features.deadlines.notifications.DeadlinesNotificationsManager
import org.stepic.droid.features.deadlines.storage.dao.DeadlinesBannerDao
import org.stepic.droid.features.deadlines.storage.operations.DeadlinesRecordOperations
import org.stepic.droid.features.deadlines.storage.structure.DbStructureDeadlinesBanner
import org.stepic.droid.features.deadlines.util.getKindOfRecord
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.util.then
import org.stepic.droid.web.CoursesMetaResponse
import org.stepic.droid.web.StepicRestLoggedService
import org.stepic.droid.web.storage.RemoteStorageService
import org.stepic.droid.web.storage.model.StorageRecordWrapped
import org.stepic.droid.web.storage.model.StorageRequest
import org.stepic.droid.web.storage.model.StorageResponse
import java.util.*
import javax.inject.Inject

@AppSingleton
class DeadlinesRepositoryImpl
@Inject
constructor(
        private val loggedService: StepicRestLoggedService,
        private val remoteStorageService: RemoteStorageService,

        private val sharedPreferenceHelper: SharedPreferenceHelper,
        private val deadlinesRecordOperations: DeadlinesRecordOperations,
        private val deadlinesNotificationsManager: DeadlinesNotificationsManager,

        private val deadlinesBannerDao: DeadlinesBannerDao
): DeadlinesRepository {
    private val gson = GsonBuilder()
            .registerTypeAdapter(Date::class.java, UTCDateAdapter())
            .create()

    private fun Single<StorageResponse>.unwrap() =
            map { it.records.first().unwrap<DeadlinesWrapper>(gson) }

    override fun createDeadlinesForCourse(deadlines: DeadlinesWrapper): Single<StorageRecord<DeadlinesWrapper>> =
            remoteStorageService
                    .createStorageRecord(createStorageRequest(deadlines)).unwrap()
                    .flatMap(deadlinesRecordOperations::saveDeadlineRecord).doOnSuccess {
                        deadlinesNotificationsManager.scheduleDeadlinesNotifications()
                    }

    override fun updateDeadlinesForCourse(record: StorageRecord<DeadlinesWrapper>): Single<StorageRecord<DeadlinesWrapper>> =
            remoteStorageService.setStorageRecord(record.id ?: 0, StorageRequest(record.wrap(gson))).unwrap()
                    .flatMap(deadlinesRecordOperations::saveDeadlineRecord).doOnSuccess {
                        deadlinesNotificationsManager.scheduleDeadlinesNotifications()
                    }

    override fun removeDeadlinesForCourseByRecordId(recordId: Long): Completable =
            remoteStorageService.removeStorageRecord(recordId) then
                    deadlinesRecordOperations.removeDeadlineRecord(recordId).doOnComplete {
                        deadlinesNotificationsManager.scheduleDeadlinesNotifications()
                    }

    override fun removeDeadlinesForCourse(courseId: Long): Completable =
            getDeadlinesForCourse(courseId).flatMapCompletable {
                deadlinesBannerDao.remove(DbStructureDeadlinesBanner.Columns.COURSE_ID, courseId.toString())
                removeDeadlinesForCourseByRecordId(it.id!!)
            }

    override fun getDeadlinesForCourse(courseId: Long): Maybe<StorageRecord<DeadlinesWrapper>> =
            remoteStorageService.getStorageRecords(1, sharedPreferenceHelper.profile?.id ?: -1, getKindOfRecord(courseId)).singleOrError()
                    .flatMapMaybe {
                        if (it.records.isNotEmpty()) {
                            Maybe.just(it.records.first().unwrap<DeadlinesWrapper>(gson))
                        } else {
                            Maybe.empty()
                        }
                    }

    override fun syncDeadlines(enrolledCourses: List<Course>?): Completable =
            deadlinesRecordOperations.removeAllDeadlineRecords() then
            (enrolledCourses?.toObservable() ?: getAllEnrolledCourses()).flatMap {
                getDeadlinesForCourse(it.id).toObservable()
            }.flatMap {
                deadlinesRecordOperations.saveDeadlineRecord(it).toObservable()
            }.doOnComplete {
                deadlinesNotificationsManager.scheduleDeadlinesNotifications()
            }.ignoreElements()

    private fun getEnrolledCourses(page: Int): Observable<CoursesMetaResponse> =
            loggedService.getEnrolledCourses(page).toObservable()

    private fun getAllEnrolledCourses(): Observable<Course> =
            getEnrolledCourses(1).concatMap {
                if (it.meta.hasNext) {
                    Observable.just(it).concatWith(getEnrolledCourses(it.meta.page + 1))
                } else {
                    Observable.just(it)
                }
            }.concatMap {
                it.courses.toObservable()
            }

    private fun createStorageRequest(deadlines: DeadlinesWrapper, recordId: Long? = null) =
            StorageRequest(StorageRecordWrapped(recordId, kind = getKindOfRecord(deadlines.course), data = gson.toJsonTree(deadlines)))


    override fun shouldShowDeadlinesBannerForCourse(courseId: Long): Single<Boolean> = Single.fromCallable {
        !deadlinesBannerDao.isInDb(courseId)
    }

    override fun hideDeadlinesBannerForCourse(courseId: Long): Completable = Completable.fromAction {
        deadlinesBannerDao.insertOrReplace(courseId)
    }
}