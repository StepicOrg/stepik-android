package org.stepik.android.cache.announcement

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.cache.announcement.dao.AnnouncementDao
import org.stepik.android.data.announcement.source.AnnouncementCacheDataSource
import org.stepik.android.domain.announcement.model.Announcement
import javax.inject.Inject

class AnnouncementCacheDataSourceImpl
@Inject
constructor(
    private val announcementDao: AnnouncementDao
) : AnnouncementCacheDataSource {
    override fun getAnnouncements(announcementIds: List<Long>): Single<List<Announcement>> =
        announcementDao.getAnnouncement(announcementIds)

    override fun saveAnnouncements(announcements: List<Announcement>): Completable =
        announcementDao.saveAnnouncements(announcements)
}