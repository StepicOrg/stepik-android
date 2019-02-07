package org.stepik.android.data.course_calendar.source

import io.reactivex.Completable
import io.reactivex.Single

interface CourseCalendarCacheDataSource {
    fun getCalendarItems(): Single<Any>
    fun saveCalendarItems(): Completable
}