package org.stepik.android.cache.calendar

import android.content.ContentResolver
import android.content.ContentValues
import android.os.Build
import android.provider.CalendarContract
import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.model.CalendarItem
import org.stepic.droid.util.AppConstants
import org.stepik.android.data.calendar.source.CalendarCacheDataSource
import java.util.*
import javax.inject.Inject

class CalendarCacheDataSourceImpl
@Inject
constructor(
    private val contentResolver: ContentResolver
) : CalendarCacheDataSource {

    override fun insertCalendarDates(): Completable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateCalendarDates(): Completable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getCalendarPrimaryItems(): Single<List<CalendarItem>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}