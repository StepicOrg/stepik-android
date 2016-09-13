package org.stepic.droid.core

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.stepic.droid.model.Course
import org.stepic.droid.model.StepikFilter
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.store.operations.DatabaseFacade
import org.stepic.droid.store.operations.Table

class FilterApplicatorImpl(
        val defaultFilter: DefaultFilter,
        val sharedPreferenceHelper: SharedPreferenceHelper) : FilterApplicator {

    override fun getFilteredFromSharedPrefs(sourceCourses: List<Course>, courseType: Table): List<Course> {
        val filters = sharedPreferenceHelper.getFilter(courseType)
        val now: Long = DateTime.now(DateTimeZone.getDefault()).millis

        val filteredList = sourceCourses.filterNotNull().filter { course ->
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

            (filters.contains(StepikFilter.RUSSIAN) && course.language?.equals("ru") ?: false
                    || filters.contains(StepikFilter.ENGLISH) && course.language?.equals("en") ?: false)
                    &&
                    (filters.contains(StepikFilter.UPCOMING) && isBeginDateInFuture
                            || filters.contains(StepikFilter.ACTIVE) && !isEnded && (beginDate != null && !isBeginDateInFuture || beginDate == null && endDate == null)
                            || filters.contains(StepikFilter.PAST) && (endDate == null && isEnded || endDate != null && !isEndDateInFuture))

        }
        return filteredList

    }

    override fun getFilteredFromDefault(sourceCourses: List<Course>, courseType: Table): List<Course>? {
        val now: Long = DateTime.now(DateTimeZone.getDefault()).millis

        val filteredList = sourceCourses.filterNotNull().filter { course ->
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

            //todo refactor: the 1st key is default for enrolled, the 2nd is default for featured
            if (courseType == Table.enrolled) {
                (defaultFilter.getDefaultEnrolled(StepikFilter.RUSSIAN) && course.language?.equals("ru") ?: false
                        || defaultFilter.getDefaultEnrolled(StepikFilter.ENGLISH) && course.language?.equals("en") ?: false)
                        &&
                        (defaultFilter.getDefaultEnrolled(StepikFilter.UPCOMING) && isBeginDateInFuture
                                || defaultFilter.getDefaultEnrolled(StepikFilter.ACTIVE) && !isEnded && (beginDate != null && !isBeginDateInFuture || beginDate == null && endDate == null)
                                || defaultFilter.getDefaultEnrolled(StepikFilter.PAST) && (endDate == null && isEnded || endDate != null && !isEndDateInFuture))
            } else {
                (defaultFilter.getDefaultFeatured(StepikFilter.RUSSIAN) && course.language?.equals("ru") ?: false
                        || defaultFilter.getDefaultFeatured(StepikFilter.ENGLISH) && course.language?.equals("en") ?: false)
                        &&
                        (defaultFilter.getDefaultFeatured(StepikFilter.UPCOMING) && isBeginDateInFuture
                                || defaultFilter.getDefaultFeatured(StepikFilter.ACTIVE) && !isEnded && (beginDate != null && !isBeginDateInFuture || beginDate == null && endDate == null)
                                || defaultFilter.getDefaultFeatured(StepikFilter.PAST) && (endDate == null && isEnded || endDate != null && !isEndDateInFuture))

            }
        }
        return filteredList
    }

}
