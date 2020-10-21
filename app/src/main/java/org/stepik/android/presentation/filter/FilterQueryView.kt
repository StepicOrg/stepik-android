package org.stepik.android.presentation.filter

import org.stepik.android.domain.filter.model.CourseListFilterQuery

interface FilterQueryView {
    fun showFilterDialog(filterQuery: CourseListFilterQuery)
}