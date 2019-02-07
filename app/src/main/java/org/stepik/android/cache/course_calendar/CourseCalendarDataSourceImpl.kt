package org.stepik.android.cache.course_calendar

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepik.android.data.course_calendar.source.CourseCalendarCacheDataSource
import javax.inject.Inject

class CourseCalendarDataSourceImpl
@Inject
constructor(
        private val databaseFacade: DatabaseFacade
) : CourseCalendarCacheDataSource {
    override fun getCalendarItems(): Single<Any> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun saveCalendarItems(): Completable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}