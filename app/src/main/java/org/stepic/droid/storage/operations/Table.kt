package org.stepic.droid.storage.operations

import org.stepic.droid.storage.structure.DbStructureEnrolledAndFeaturedCourses

enum class Table(val storeName: String) {
    enrolled(DbStructureEnrolledAndFeaturedCourses.ENROLLED_COURSES),
    featured(DbStructureEnrolledAndFeaturedCourses.FEATURED_COURSES)
}