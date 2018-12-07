package org.stepic.droid.core.presenters

import android.support.annotation.WorkerThread
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.concurrency.MainHandler
import org.stepic.droid.concurrency.SingleThreadExecutor
import org.stepic.droid.core.FilterApplicator
import org.stepic.droid.core.FirstCoursePoster
import org.stepic.droid.core.earlystreak.contract.EarlyStreakPoster
import org.stepic.droid.core.presenters.contracts.CoursesView
import org.stepic.droid.di.course_list.CourseListScope
import org.stepic.droid.features.deadlines.repository.DeadlinesRepository
import org.stepic.droid.model.CourseListType
import org.stepic.droid.model.CourseReviewSummary
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepic.droid.util.CourseUtil
import org.stepic.droid.util.DateTimeHelper
import org.stepic.droid.util.RWLocks
import org.stepic.droid.web.Api
import org.stepik.android.model.Course
import org.stepik.android.model.Meta
import org.stepik.android.model.Progress
import org.stepik.android.model.UserCourse
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

@CourseListScope
class PersistentCourseListPresenter
@Inject constructor(
        private val databaseFacade: DatabaseFacade,
        private val singleThreadExecutor: SingleThreadExecutor,
        private val mainHandler: MainHandler,
        private val api: Api,
        private val filterApplicator: FilterApplicator,
        private val sharedPreferenceHelper: SharedPreferenceHelper,
        private val earlyStreakPoster: EarlyStreakPoster,
        private val firstCoursePoster: FirstCoursePoster,

        private val deadlinesRepository: DeadlinesRepository,
        private val analytic: Analytic
) : PresenterBase<CoursesView>() {

    companion object {
        //if hasNextPage & < MIN_COURSES_ON_SCREEN -> load next page
        private const val MIN_COURSES_ON_SCREEN = 5
        private const val MAX_CURRENT_NUMBER_OF_TASKS = 2
        private const val SEVEN_DAYS_MILLIS = 7 * 24 * 60 * 60 * 1000L
        private const val MILLIS_IN_SECOND = 1000L
    }

    private val currentPage = AtomicInteger(1)
    private val hasNextPage = AtomicBoolean(true)
    private var currentNumberOfTasks: Int = 0 //only main thread
    private val isEmptyCourses = AtomicBoolean(false)

    fun restoreState() {
        if (isEmptyCourses.get() && !hasNextPage.get()) {
            view?.showEmptyCourses()
        }
    }

    /**
     * 1) Show from cache, if not empty (hide progress)
     * 2) Load from internet (if fail -> show)
     * 3) Save to db
     * 4) show from cache (all states)
     */
    fun downloadData(courseType: CourseListType) {
        downloadData(courseType, isRefreshing = false)
    }

    private fun downloadData(courseType: CourseListType, isRefreshing: Boolean, isLoadMore: Boolean = false) {
        if (currentNumberOfTasks >= MAX_CURRENT_NUMBER_OF_TASKS) {
            return
        }
        currentNumberOfTasks++
        singleThreadExecutor.execute {
            try {
                downloadDataPlain(isRefreshing, isLoadMore, courseType)
            } finally {
                mainHandler.post {
                    currentNumberOfTasks--
                }
            }
        }
    }

    @WorkerThread
    private fun downloadDataPlain(isRefreshing: Boolean, isLoadMore: Boolean, courseType: CourseListType) {
        if (!isLoadMore) {
            mainHandler.post {
                view?.showLoading()
            }
            showFromDatabase(courseType)
        } else if (hasNextPage.get()) {
            mainHandler.post {
                view?.showLoading()
            }
        }

        while (hasNextPage.get()) {
            val coursesFromInternet: List<Course>? = try {
                if (courseType == CourseListType.FEATURED) {
                    val response = api.getPopularCourses(currentPage.get()).blockingGet()
                    handleMeta(response.meta)
                    response.courses
                } else {
                    val allMyCourses = arrayListOf<Course>()
                    while (hasNextPage.get()) {
                        val page = currentPage.get()
                        val coursesOrder = api.getUserCourses(page)
                                .blockingGet()
                                .apply { handleMeta(meta) }
                                .userCourse
                                .map(UserCourse::course)

                        val coursesResponse = api.getCoursesReactive(1, coursesOrder.toLongArray())
                                .blockingGet()
                        val courses = coursesResponse
                                .courses
                                .sortedBy { coursesOrder.indexOf(it.id) }
                        
                        allMyCourses.addAll(courses)
                    }
                    deadlinesRepository.syncDeadlines(allMyCourses).blockingAwait()
                    analytic.setCoursesCount(allMyCourses.size)
                    allMyCourses
                }
            } catch (ex: Exception) {
                null
            }?.distinctBy { it.id }

            if (coursesFromInternet == null) {
                mainHandler.post {
                    firstCoursePoster.postConnectionError()
                    view?.showConnectionProblem()
                }
                break
            }

            if (courseType == CourseListType.ENROLLED) {
                val progressIds = coursesFromInternet.map { it.progress }.toTypedArray()
                val progresses: List<Progress>? = try {
                    api.getProgresses(progressIds).execute().body()?.progresses
                } catch (exception: Exception) {
                    //ok show without progresses
                    null
                }
                progresses?.forEach {
                    databaseFacade.addProgress(progress = it)
                }
            }

            val reviewSummaryIds = coursesFromInternet.map { it.reviewSummary }.toLongArray()
            val reviews: List<CourseReviewSummary>? = try {
                api.getCourseReviews(reviewSummaryIds).blockingGet().courseReviewSummaries
            } catch (exception: Exception) {
                //ok show without new ratings
                null
            }
            CourseUtil.applyReviewsToCourses(reviews, coursesFromInternet)

            try {
                //this lock need for not saving enrolled courses to database after user click logout
                RWLocks.ClearEnrollmentsLock.writeLock().lock()
                if (sharedPreferenceHelper.authResponseFromStore != null || courseType == CourseListType.FEATURED) {
                    if (isRefreshing) {
                        if (courseType == CourseListType.FEATURED && currentPage.get() == 2) {
                            databaseFacade.dropFeaturedCourses()
                        } else if (courseType == CourseListType.ENROLLED) {
                            databaseFacade.dropEnrolledCourses()
                        }
                    }

                    databaseFacade.addCourseList(courseType, coursesFromInternet)
                }
            } finally {
                RWLocks.ClearEnrollmentsLock.writeLock().unlock()
            }

            val allCourses = databaseFacade.getAllCourses(courseType).toMutableList()

            val coursesForShow: List<Course> = handleCoursesWithType(allCourses, courseType)
            if (coursesForShow.size < MIN_COURSES_ON_SCREEN && hasNextPage.get()) {
                //try to load next in loop
            } else {
                mainHandler.post {
                    postFirstCourse(courseType, coursesForShow)
                    if (coursesForShow.isEmpty()) {
                        isEmptyCourses.set(true)
                        view?.showEmptyCourses()
                    } else {
                        view?.showCourses(coursesForShow)
                    }
                }
                break
            }
        }
    }

    private fun handleMeta(meta: Meta) {
        hasNextPage.set(meta.hasNext)
        if (hasNextPage.get()) {
            currentPage.set(meta.page + 1) // page for next loading
        }
    }

    @WorkerThread
    private fun showFromDatabase(courseType: CourseListType) {
        val coursesBeforeLoading = databaseFacade.getAllCourses(courseType)
        val coursesForShow = handleCoursesWithType(coursesBeforeLoading, courseType)

        if (coursesForShow.isNotEmpty()) {
            mainHandler.post {
                view?.showCourses(coursesForShow)
                postFirstCourse(courseType, coursesForShow)
            }
        }
    }

    private fun postFirstCourse(courseType: CourseListType, coursesForShow: List<Course>) {
        if (courseType != CourseListType.ENROLLED) {
            return
        }
        val course = coursesForShow.find {
            it.isActive && it.sections?.isNotEmpty() ?: false
        }
        firstCoursePoster.postFirstCourse(course)
    }

    private fun handleCoursesWithType(courses: List<Course>, courseType: CourseListType?): List<Course> =
            when (courseType) {
                CourseListType.ENROLLED -> {
                    val progressMap = getProgressesFromDb(courses)
                    CourseUtil.applyProgressesToCourses(progressMap, courses)
                    postLastActive(courses.firstOrNull(), progressMap)
                    courses
                }
                CourseListType.FEATURED -> {
                    filterApplicator.filterCourses(courses)
                }
                null -> courses
            }

    private fun postLastActive(course: Course?, progressMap: Map<String?, Progress>) {

        val lastViewed = progressMap[course?.progress]?.lastViewed?.toLongOrNull()

        if (lastViewed != null && isViewedDuringLast7Days(lastViewed)) {
            mainHandler.post {
                earlyStreakPoster.showStreakSuggestion()
            }
        }
    }

    private fun isViewedDuringLast7Days(lastViewed: Long): Boolean =
            DateTimeHelper.isAfterNowUtc(lastViewed * MILLIS_IN_SECOND + SEVEN_DAYS_MILLIS)

    fun refreshData(courseType: CourseListType) {
        if (currentNumberOfTasks >= MAX_CURRENT_NUMBER_OF_TASKS) {
            return
        }
        currentPage.set(1)
        hasNextPage.set(true)
        downloadData(courseType, isRefreshing = true)
    }

    @WorkerThread
    private fun getProgressesFromDb(courses: List<Course>): Map<String?, Progress> {
        val progressIds = courses.mapNotNull {
            it.progress
        }
        return databaseFacade.getProgresses(progressIds).associateBy { it.id }
    }


    fun loadMore(courseType: CourseListType) {
        downloadData(courseType, isRefreshing = false, isLoadMore = true)
    }
}
