package org.stepic.droid.store.operations

import org.stepic.droid.store.structure.DBStructureCourses

enum class Table(val storeName: String) {
    enrolled(DBStructureCourses.ENROLLED_COURSES),
    featured(DBStructureCourses.FEATURED_COURSES)
}