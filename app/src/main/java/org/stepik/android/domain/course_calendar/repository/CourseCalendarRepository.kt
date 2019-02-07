package org.stepik.android.domain.course_calendar.repository

import io.reactivex.Completable
import io.reactivex.Single

interface CourseCalendarRepository {
    fun getCalendarItems(): Single<Any>
    fun saveCalendarItems(): Completable
}