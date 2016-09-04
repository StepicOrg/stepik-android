package org.stepic.droid.core.presenters

import android.os.Bundle
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.concurrency.IMainHandler
import org.stepic.droid.core.FilterApplicator
import org.stepic.droid.core.presenters.contracts.CoursesView
import org.stepic.droid.model.Course
import org.stepic.droid.model.StepikFilter
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.store.operations.DatabaseFacade
import org.stepic.droid.store.operations.Table
import org.stepic.droid.web.CoursesStepicResponse
import org.stepic.droid.web.IApi
import retrofit.Response
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class PersistentCourseListPresenter(
        val analytic: Analytic,
        val databaseFacade: DatabaseFacade,
        val threadPoolExecutor: ThreadPoolExecutor,
        val mainHandler: IMainHandler,
        val api: IApi,
        val filterApplicator: FilterApplicator,
        val sharedPreferenceHelper: SharedPreferenceHelper
) : PresenterBase<CoursesView>() {

    var currentPage = AtomicInteger(1);
    var hasNextPage = AtomicBoolean(true)
    var isLoading = AtomicBoolean(false)
    var isEmptyCourses = AtomicBoolean(false)

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
        if (isLoading.get() || !hasNextPage.get()) return
        isLoading.set(true)

        threadPoolExecutor.execute {
            val coursesBeforeLoading = databaseFacade.getAllCourses(courseType).filterNotNull()
            if (coursesBeforeLoading.isNotEmpty() && currentPage.get() == 1) {
                val filteredCourseList: List<Course>
                if (!applyFilter && !sharedPreferenceHelper.getFilter(courseType).contains(StepikFilter.PERSISTENT)) {
                    filteredCourseList = filterApplicator.getFilteredFromDefault(coursesBeforeLoading, courseType)
                } else {
                    filteredCourseList = filterApplicator.getFilteredFromSharedPrefs(coursesBeforeLoading, courseType)
                }
                if (filteredCourseList.isNotEmpty()) {
                    mainHandler.post {
                        view?.showCourses(filteredCourseList)
                    }
                } else {
                    mainHandler.post { view?.showLoading() }
                }
            } else {
                mainHandler.post { view?.showLoading() }
            }

            while (hasNextPage.get()) {
                val response: Response<CoursesStepicResponse>?
                try {
                    if (courseType == Table.featured) {
                        response = api.getFeaturedCourses(currentPage.get()).execute()
                    } else {
                        response = api.getEnrolledCourses(currentPage.get()).execute()
                    }
                } catch (ex: Exception) {
                    response = null
                }

                if (response != null && response.isSuccess) {
                    val coursesFromInternet = response.body().courses

                    coursesFromInternet.filterNotNull().forEach {
                        databaseFacade.addCourse(it, courseType)
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
                    if (filteredCourseList.isEmpty() && hasNextPage.get()) {
                        //try to load next in loop
                    } else {
                        mainHandler.post {
                            if (filteredCourseList.isEmpty()) {
                                isEmptyCourses.set(true)
                                view?.showEmptyCourses()
                            } else {
                                view?.showCourses(filteredCourseList)
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

    fun refreshData(courseType: Table, applyFilter: Boolean) {
        if (isLoading.get()) return
        currentPage.set(1);
        hasNextPage.set(true)
        downloadData(courseType, applyFilter)
    }

}
