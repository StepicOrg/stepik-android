package org.stepic.droid.storage.operations

import org.stepic.droid.storage.structure.DBStructureCourses

enum class Table(val storeName: String) {
    enrolled(DBStructureCourses.ENROLLED_COURSES),
    featured(DBStructureCourses.FEATURED_COURSES)
}