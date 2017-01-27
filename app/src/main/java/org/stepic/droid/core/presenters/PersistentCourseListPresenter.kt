package org.stepic.droid.core.presenters

import android.os.Bundle
import android.support.annotation.WorkerThread
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.concurrency.IMainHandler
import org.stepic.droid.core.FilterApplicator
import org.stepic.droid.core.presenters.contracts.CoursesView
import org.stepic.droid.model.Course
import org.stepic.droid.model.StepikFilter
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.store.operations.DatabaseFacade
import org.stepic.droid.store.operations.Table
import org.stepic.droid.util.RWLocks
import org.stepic.droid.web.CoursesStepicResponse
import org.stepic.droid.web.IApi
import retrofit.Response
import java.util.*
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.comparisons.compareBy

class PersistentCourseListPresenter(
        val analytic: Analytic,
        val databaseFacade: DatabaseFacade,
        val threadPoolExecutor: ThreadPoolExecutor,
        val mainHandler: IMainHandler,
        val api: IApi,
        val filterApplicator: FilterApplicator,
        val sharedPreferenceHelper: SharedPreferenceHelper
) : PresenterBase<CoursesView>() {

    val currentPage = AtomicInteger(1);
    val hasNextPage = AtomicBoolean(true)
    val isLoading = AtomicBoolean(false)
    val isEmptyCourses = AtomicBoolean(false)

//    val isHandlingUpdatingOrder = AtomicBoolean(false)

    //if hasNextPage & <MIN_COURSES_ON_SCREEN -> load next page
    val MIN_COURSES_ON_SCREEN = 5

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
        if (isLoading.compareAndSet(false, true)) {
            threadPoolExecutor.execute {
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
                            api.getFeaturedCourses(currentPage.get()).execute()
                        } else {
                            api.getEnrolledCourses(currentPage.get()).execute()
                        }
                    } catch (ex: Exception) {
                        null
                    }

                    if (response != null && response.isSuccess) {
                        val coursesFromInternet = response.body().courses

                        try {
                            //this lock need for not saving enrolled courses to database after user click logout
                            RWLocks.ClearEnrollmentsLock.writeLock().lock()
                            if (sharedPreferenceHelper.authResponseFromStore != null || courseType == Table.featured) {
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
                        if ((filteredCourseList.size < MIN_COURSES_ON_SCREEN || isRefreshing) && hasNextPage.get()) {
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
                            break;
                        }
                    } else {
                        mainHandler.post {
                            view?.showConnectionProblem()
                        }
                        break;
                    }
                }
                isLoading.set(false)
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
            } else {
                mainHandler.post { view?.showLoading() }
            }
        } else {
            mainHandler.post { view?.showLoading() }
        }
    }

    fun reportCurrentFiltersToAnalytic(courseType: Table) {
        threadPoolExecutor.execute {
            val enumSetOfFilters = sharedPreferenceHelper.getFilter(courseType)
            val bundle = Bundle()
            enumSetOfFilters.forEach {
                bundle.putBoolean(it.toString(), true)
            }
            analytic.reportEvent(Analytic.Filters.FILTER_APPLIED_IN_INTERFACE_WITH_PARAMS)
        }
    }

    fun refreshData(courseType: Table, applyFilter: Boolean, allPAges: Boolean) {
        if (isLoading.get()) return
        currentPage.set(1);
        hasNextPage.set(true)
        downloadData(courseType, applyFilter, isRefreshing = allPAges)
    }


    @WorkerThread
    private fun sortByLastAction(courses: List<Course>): List<Course> {
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

    public fun loadMore(courseType: Table, needFilter: Boolean) {
        downloadData(courseType, needFilter, isRefreshing = false, isLoadMore = true)
    }

//    @MainThread
//    fun updateOrderLastInteraction(courseType: Table, courses: List<Course>) {
//        if (courseType != Table.enrolled || courses.isEmpty()) {
//            return
//        }
//        if (isHandlingUpdatingOrder.compareAndSet(false, true)) {
//            threadPoolExecutor.execute {
//                try {
//                    val lastInteractedOrder   =  sortByLastAction(courses)
//                    mainHandler.post {
//                        view?.showCourses()
//                    }
//                } finally {
//                    isHandlingUpdatingOrder.set(false)
//                }
//            }
//        }
//
//    }

}
