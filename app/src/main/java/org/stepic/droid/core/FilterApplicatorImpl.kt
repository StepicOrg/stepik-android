package org.stepic.droid.core

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.stepic.droid.model.Course
import org.stepic.droid.model.StepikFilter
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.store.operations.Table
import javax.inject.Inject

class FilterApplicatorImpl
@Inject constructor(private val defaultFilter: DefaultFilter,
                    private val sharedPreferenceHelper: SharedPreferenceHelper) : FilterApplicator {

    override fun getFilteredFromSharedPrefs(sourceCourses: List<Course>, courseType: Table): List<Course> {
        val filters = sharedPreferenceHelper.getFilter(courseType)
        return resolveFiltersForList(sourceCourses, filters)
    }


    override fun getFilteredFromDefault(sourceCourses: List<Course>, courseType: Table): List<Course>? {
        val filters = if (courseType == Table.enrolled) {
            StepikFilter.values().filter { defaultFilter.getDefaultEnrolled(it) }.toSet()
        } else {
            StepikFilter.values().filter { defaultFilter.getDefaultFeatured(it) }.toSet()
        }

        return resolveFiltersForList(sourceCourses, filters)
    }

    private fun resolveFiltersForList(sourceCourses: List<Course>, filters: Set<StepikFilter>): List<Course> {
        //local helper functions:
        fun resolveFilters(course: Course, now: Long, applyFilters: (Course, endDate: Long?, isAfterBeginOrNotStartable: Boolean, isBeginDateInFuture: Boolean, isEndDateInFuture: Boolean, isEnded: Boolean, filterSet: Set<StepikFilter>) -> Boolean, filterSet: Set<StepikFilter>): Boolean {
            var beginDate: Long? = null
            course.begin_date?.let {
                beginDate = DateTime(it).millis
            }

            var endDate: Long? = null
            course.end_date?.let {
                endDate = DateTime(it).millis
            }

            var isEnded: Boolean = false
            course.last_deadline?.let {
                val lastDeadlineMillis = DateTime(it).millis
                if (now > lastDeadlineMillis) {
                    isEnded = true
                }
            }

            val isBeginDateInFuture: Boolean = beginDate?.compareTo(now) ?: -1 > 0
            val isEndDateInFuture: Boolean = endDate?.compareTo(now) ?: -1 > 0

            val isAfterBeginOrNotStartable = (beginDate != null && !isBeginDateInFuture || beginDate == null)
            return applyFilters.invoke(course, endDate, isAfterBeginOrNotStartable, isBeginDateInFuture, isEndDateInFuture, isEnded, filterSet)

        }

        fun applyFiltersForSet(course: Course, endDate: Long?, isAfterBeginOrNotStartable: Boolean, isBeginDateInFuture: Boolean, isEndDateInFuture: Boolean, isEnded: Boolean, filters: Set<StepikFilter>): Boolean {
            return (
                    filters.contains(StepikFilter.RUSSIAN) && course.language?.equals("ru") ?: false
                            || filters.contains(StepikFilter.ENGLISH) && course.language?.equals("en") ?: false
                            || (filters.contains(StepikFilter.RUSSIAN) && filters.contains(StepikFilter.ENGLISH))
                    )

                    &&
                    (filters.contains(StepikFilter.UPCOMING) && isBeginDateInFuture
                            || filters.contains(StepikFilter.ACTIVE) && (!isEnded && isAfterBeginOrNotStartable || endDate != null && isEndDateInFuture && !isBeginDateInFuture)
                            || filters.contains(StepikFilter.PAST) && (endDate == null && isEnded || endDate != null && !isEndDateInFuture))
        }

        //logic

        val possibleFilterSize = StepikFilter.values().size
        if (possibleFilterSize == filters.size
                || possibleFilterSize - 1 == filters.size && !filters.contains(StepikFilter.PERSISTENT)) {
            // if all filters are chosen or all except persistent is chosen -> do not filter
            return sourceCourses
        }

        val now: Long = DateTime.now(DateTimeZone.getDefault()).millis
        val filteredList = sourceCourses.filterNotNull().filter { course ->
            resolveFilters(course, now, ::applyFiltersForSet, filters)
        }
        return filteredList
    }

}
