package org.stepic.droid.core.presenters

import android.support.annotation.WorkerThread
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.concurrency.MainHandler
import org.stepic.droid.concurrency.SingleThreadExecutor
import org.stepic.droid.core.FilterApplicator
import org.stepic.droid.core.presenters.contracts.CoursesView
import org.stepic.droid.di.course_list.CourseListScope
import org.stepic.droid.model.Course
import org.stepic.droid.model.StepikFilter
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepic.droid.storage.operations.Table
import org.stepic.droid.util.RWLocks
import org.stepic.droid.web.Api
import org.stepic.droid.web.CoursesStepicResponse
import retrofit2.Response
import timber.log.Timber
import java.util.*
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

@CourseListScope
class PersistentCourseListPresenter
@Inject constructor(
        private val analytic: Analytic,
        private val databaseFacade: DatabaseFacade,
        private val singleThreadExecutor: SingleThreadExecutor,
        private val threadPoolExecutor: ThreadPoolExecutor,
        private val mainHandler: MainHandler,
        private val api: Api,
        private val filterApplicator: FilterApplicator,
        private val sharedPreferenceHelper: SharedPreferenceHelper
) : PresenterBase<CoursesView>() {

    companion object {
        //if hasNextPage & <MIN_COURSES_ON_SCREEN -> load next page
        private const val MIN_COURSES_ON_SCREEN = 5
        private const val MAX_CURRENT_NUMBER_OF_TASKS = 2
    }

    private val currentPage = AtomicInteger(1);
    private val hasNextPage = AtomicBoolean(true)
    private var currentNumberOfTasks: Int = 0 //only main thread
    private val isEmptyCourses = AtomicBoolean(false)

    fun restoreState() {
        if (isEmptyCourses.get() && !hasNextPage.get()) {
            view?.showEmptyCourses()
        }
    }

    /**
     * 1) Show from cache, if not empty (hide progress).
     * 2) Load from internet (if fail -> show)
     * 3) Save to db
     * 4) show from cache. (all states)
     */
    fun downloadData(courseType: Table, applyFilter: Boolean) {
        downloadData(courseType, applyFilter, isRefreshing = false)
    }

    private fun downloadData(courseType: Table, applyFilter: Boolean, isRefreshing: Boolean, isLoadMore: Boolean = false) {
        if (currentNumberOfTasks >= MAX_CURRENT_NUMBER_OF_TASKS) {
            return
        }
        currentNumberOfTasks++
        Timber.d("load more tasks = $currentNumberOfTasks") //here 1 or 2, not more
        if (hasNextPage.get()) {
            view?.showLoading()
        }
        singleThreadExecutor.execute {
            try {
                Timber.d("load more start downloading ${Thread.currentThread()}")
                downloadDataPlain(isRefreshing, isLoadMore, applyFilter, courseType)
                Timber.d("load more end downloading ${Thread.currentThread()}")
            } finally {
                mainHandler.post {
                    currentNumberOfTasks--
                }
            }
        }
    }

    @WorkerThread
    private fun downloadDataPlain(isRefreshing: Boolean, isLoadMore: Boolean, applyFilter: Boolean, courseType: Table) {
        if (!isRefreshing && !isLoadMore) {
            getFromDatabaseAndShow(applyFilter, courseType)
        } else if (hasNextPage.get()) {
            mainHandler.post {
                view?.showLoading()
            }
        }

        while (hasNextPage.get()) {
            val response: Response<CoursesStepicResponse>? = try {
                if (courseType == Table.featured) {
                    api.getPopularCourses(currentPage.get()).execute()
                } else {
                    api.getEnrolledCourses(currentPage.get()).execute()
                }
            } catch (ex: Exception) {
                null
            }

            if (response != null && response.isSuccessful) {
                val coursesFromInternet = response.body().courses
                try {
                    //this lock need for not saving enrolled courses to database after user click logout
                    RWLocks.ClearEnrollmentsLock.writeLock().lock()
                    if (sharedPreferenceHelper.authResponseFromStore != null || courseType == Table.featured) {
                        if (isRefreshing && currentPage.get() == 1) {
                            if (courseType == Table.featured) {
                                databaseFacade.dropFeaturedCourses()
                            } else if (courseType == Table.enrolled) {
                                databaseFacade.dropEnrolledCourses()
                            }
                        }

                        coursesFromInternet.filterNotNull().forEach {
                            databaseFacade.addCourse(it, courseType)
                        }
                    }
                } finally {
                    RWLocks.ClearEnrollmentsLock.writeLock().unlock()
                }


                hasNextPage.set(response.body().meta.has_next)
                if (hasNextPage.get()) {
                    currentPage.set(response.body().meta.page + 1) // page for next loading
                }

                val allCourses = databaseFacade.getAllCourses(courseType)

                val filteredCourseList: List<Course>
                if (!applyFilter && !sharedPreferenceHelper.getFilter(courseType).contains(StepikFilter.PERSISTENT)) {
                    filteredCourseList = filterApplicator.getFilteredFromDefault(allCourses, courseType)
                } else {
                    filteredCourseList = filterApplicator.getFilteredFromSharedPrefs(allCourses, courseType)
                }
                if ((filteredCourseList.size < MIN_COURSES_ON_SCREEN) && hasNextPage.get()) {
                    //try to load next in loop
                } else {
                    val coursesForShow = if (courseType == Table.enrolled) {
                        sortByLastAction(filteredCourseList)
                    } else {
                        filteredCourseList
                    }
                    mainHandler.post {
                        if (coursesForShow.isEmpty()) {
                            isEmptyCourses.set(true)
                            view?.showEmptyCourses()
                        } else {
                            view?.showCourses(coursesForShow)
                        }
                    }
                    break
                }
            } else {
                mainHandler.post {
                    view?.showConnectionProblem()
                }
                break
            }
        }
    }

    private fun getFromDatabaseAndShow(applyFilter: Boolean, courseType: Table) {
        val coursesBeforeLoading = databaseFacade.getAllCourses(courseType).filterNotNull()
        if (coursesBeforeLoading.isNotEmpty()) {
            val filteredCourseList: List<Course>
            if (!applyFilter && !sharedPreferenceHelper.getFilter(courseType).contains(StepikFilter.PERSISTENT)) {
                filteredCourseList = filterApplicator.getFilteredFromDefault(coursesBeforeLoading, courseType)
            } else {
                filteredCourseList = filterApplicator.getFilteredFromSharedPrefs(coursesBeforeLoading, courseType)
            }
            val coursesForShow = if (courseType == Table.enrolled) {
                sortByLastAction(filteredCourseList)
            } else {
                filteredCourseList
            }
            if (coursesForShow.isNotEmpty()) {
                mainHandler.post {
                    view?.showCourses(coursesForShow)
                }
            } else if (hasNextPage.get()) {
                mainHandler.post { view?.showLoading() }
            }
        } else {
            if (hasNextPage.get()) {
                //do not show loading, if we have not the next page
                //loading is useless in this case
                mainHandler.post { view?.showLoading() }
            }
        }
    }

    fun refreshData(courseType: Table, applyFilter: Boolean, isRefreshing: Boolean) {
        if (currentNumberOfTasks >= MAX_CURRENT_NUMBER_OF_TASKS) {
            return
        }
        currentPage.set(1);
        hasNextPage.set(true)
        downloadData(courseType, applyFilter, isRefreshing = isRefreshing)
    }

    @WorkerThread
    private fun sortByLastAction(courses: List<Course>): MutableList<Course> {
        val result = ArrayList<Course>(courses.size)
        val localLastStepsList = databaseFacade.getAllLocalLastCourseInteraction()
        val sortedPersistentLastStepCourseIds = localLastStepsList
                .filterNotNull()
                .filter { it.timestamp > 0 }
                .toSortedSet(compareBy { it.timestamp.times(-1L) })
        val coursesMap = courses.associateBy { it.courseId }
        val usedCourses = HashSet<Long>()
        sortedPersistentLastStepCourseIds.forEach {
            val course = coursesMap[it.courseId]
            if (course != null) {
                result.add(course)
                usedCourses.add(course.courseId)
            }
        }

        courses.forEach {
            if (!usedCourses.contains(it.courseId)) {
                result.add(it)
            }
        }

        return result
    }

    fun loadMore(courseType: Table, needFilter: Boolean) {
        downloadData(courseType, needFilter, isRefreshing = false, isLoadMore = true)
    }
}
