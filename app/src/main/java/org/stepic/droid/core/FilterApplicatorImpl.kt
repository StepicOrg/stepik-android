package org.stepic.droid.core

import org.stepik.android.model.Course
import org.stepic.droid.model.StepikFilter
import org.stepic.droid.preferences.SharedPreferenceHelper
import javax.inject.Inject

class FilterApplicatorImpl
@Inject constructor(private val sharedPreferenceHelper: SharedPreferenceHelper) : FilterApplicator {

    override fun filterCourses(sourceCourses: List<Course>): List<Course> =
            resolveFiltersForList(sourceCourses, sharedPreferenceHelper.filterForFeatured)

    private fun resolveFiltersForList(sourceCourses: List<Course>, filters: Set<StepikFilter>): List<Course> {
        if (StepikFilter.values().size == filters.size) {
            // if all filters are chosen or all except persistent is chosen -> do not filter
            return sourceCourses
        }

        return sourceCourses.filter { course ->
            applyFiltersForSet(course, filters)
        }
    }

    private fun applyFiltersForSet(course: Course, filters: Set<StepikFilter>): Boolean {
        return (
                filters.contains(StepikFilter.RUSSIAN) && course.language == StepikFilter.RUSSIAN.language
                        || filters.contains(StepikFilter.ENGLISH) && course.language == StepikFilter.ENGLISH.language
                        || (filters.contains(StepikFilter.RUSSIAN) && filters.contains(StepikFilter.ENGLISH)
                        || (!filters.contains(StepikFilter.RUSSIAN) && !filters.contains(StepikFilter.ENGLISH)))
                )
    }

}
