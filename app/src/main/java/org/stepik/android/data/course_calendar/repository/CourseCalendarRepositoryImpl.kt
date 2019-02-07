package org.stepik.android.data.course_calendar.repository

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.data.course_calendar.source.CourseCalendarCacheDataSource
import org.stepik.android.domain.course_calendar.repository.CourseCalendarRepository
import javax.inject.Inject

class CourseCalendarRepositoryImpl
@Inject
constructor(
    private val courseCalendarCacheDataSource: CourseCalendarCacheDataSource
) : CourseCalendarRepository {
    override fun getCalendarItems(): Single<Any> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun saveCalendarItems(): Completable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}