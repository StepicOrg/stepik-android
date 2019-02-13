package org.stepik.android.view.injection.calendar

import dagger.Binds
import dagger.Module
import org.stepik.android.cache.calendar.CalendarCacheDataSourceImpl
import org.stepik.android.data.calendar.repository.CalendarRepositoryImpl
import org.stepik.android.data.calendar.source.CalendarCacheDataSource
import org.stepik.android.domain.calendar.repository.CalendarRepository

@Module
abstract class CalendarDataModule {

    @Binds
    internal abstract fun bindCalendarRepository(
            calendarRepositoryImpl: CalendarRepositoryImpl
    ): CalendarRepository

    @Binds
    internal abstract fun bindCalendarCacheDataSource(
            calendarCacheDataSourceImpl: CalendarCacheDataSourceImpl
    ): CalendarCacheDataSource
}