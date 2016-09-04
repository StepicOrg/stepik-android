package org.stepic.droid.core.presenters

import org.stepic.droid.analytic.Analytic
import org.stepic.droid.concurrency.IMainHandler
import org.stepic.droid.core.FilterApplicator
import org.stepic.droid.core.presenters.contracts.FilterForCoursesView
import org.stepic.droid.model.Course
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.store.operations.DatabaseFacade
import org.stepic.droid.store.operations.Table
import java.util.concurrent.ThreadPoolExecutor

@Deprecated("This class is useless or need remaking")
class FilterForCoursesPresenter(
        val sharedPreferenceHelper: SharedPreferenceHelper,
        val analytic: Analytic,
        val threadPoolExecutor: ThreadPoolExecutor,
        val mainHandler: IMainHandler,
        val databaseFacade: DatabaseFacade,
        val filterApplicator: FilterApplicator) : PresenterBase<FilterForCoursesView>() {

    fun applyFiltersImmediate(courses: List<Course?>): List<Course> {
        //todo: it can be too long, make with callback
        return getListFilteredBySharedPrefs(courses)
    }

    fun tryApplyFilters(type: Table) {
        if (sharedPreferenceHelper.isFilterChangedFromLastCall) {
            onNeedUpdateFilters(type)
        } else {
            onFiltersNotChanged()
        }
    }

    private fun onNeedUpdateFilters(type: Table) {
        analytic.reportEvent(Analytic.Filters.FILTERS_NEED_UPDATE)
        view?.clearAndShowLoading()
        threadPoolExecutor.execute {
            //todo optimization: change to specific sql query
            val courseList = databaseFacade.getAllCourses(type)

            val filteredList = getListFilteredBySharedPrefs(courseList)

            mainHandler.post {
                view?.showFilteredCourses(filteredList)
            }
        }
    }

    private fun getListFilteredBySharedPrefs(courseList: List<Course?>): List<Course> {
        return filterApplicator.getFilteredFromSharedPrefs(courseList, Table.enrolled)//change resolving of enrolled
    }

    private fun onFiltersNotChanged() {
        //do nothing now
        analytic.reportEvent(Analytic.Filters.FILTERS_NOT_CHANGED)
    }
}