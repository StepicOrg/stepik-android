package org.stepic.droid.core.presenters

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.concurrency.IMainHandler
import org.stepic.droid.core.presenters.contracts.FilterForCoursesView
import org.stepic.droid.model.StepikFilter
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.store.operations.DatabaseFacade
import java.util.concurrent.ThreadPoolExecutor

class FilterForCoursesPresenter(
        val sharedPreferenceHelper: SharedPreferenceHelper,
        val analytic: Analytic,
        val threadPoolExecutor: ThreadPoolExecutor,
        val mainHandler: IMainHandler,
        val databaseFacade: DatabaseFacade) : PresenterBase<FilterForCoursesView>() {

    fun tryApplyFilters(type: DatabaseFacade.Table) {
        if (sharedPreferenceHelper.isFilterChangedFromLastCall) {
            onNeedUpdateFilters(type)
        } else {
            onFiltersNotChanged()
        }
    }

    private fun onNeedUpdateFilters(type: DatabaseFacade.Table) {
        analytic.reportEvent(Analytic.Filters.FILTERS_NOT_CHANGED)
        view?.clearAndShowLoading()
        threadPoolExecutor.execute {
            val filters = sharedPreferenceHelper.filter

            //todo optimization: change to specific sql query
            val courseList = databaseFacade.getAllCourses(type)
            val now: Long = DateTime.now(DateTimeZone.getDefault()).millis

            val filteredList = courseList.filter { course ->
                var beginDate: Long? = null
                course?.begin_date?.let {
                    beginDate = DateTime(it).millis
                }

                var endDate: Long? = null
                course?.end_date?.let {
                    endDate = DateTime(it).millis
                }

                var isEnded: Boolean = false
                course?.last_deadline?.let {
                    val lastDeadlineMillis = DateTime(it).millis
                    if (now > lastDeadlineMillis) {
                        isEnded = true
                    }
                }

                val isBeginDateInFuture: Boolean = beginDate?.compareTo(now) ?: -1 > 0
                val isEndDateInFuture: Boolean = endDate?.compareTo(now) ?: -1 > 0


                (filters.contains(StepikFilter.RUSSIAN) && course?.language?.equals("ru") ?: false
                        || filters.contains(StepikFilter.ENGLISH) && course?.language?.equals("en") ?: false)
                        &&
                        (filters.contains(StepikFilter.UPCOMING) && isBeginDateInFuture
                                || filters.contains(StepikFilter.ACTIVE) && !isEnded && (beginDate != null && !isBeginDateInFuture || beginDate == null && endDate == null)
                                || filters.contains(StepikFilter.PAST) && (endDate == null && isEnded || endDate != null && !isEndDateInFuture))
            }


            mainHandler.post {
                view?.showFilteredCourses(filteredList)
            }

        }

    }

    private fun onFiltersNotChanged() {
        //do nothing now
        analytic.reportEvent(Analytic.Filters.FILTERS_NOT_CHANGED)
    }
}