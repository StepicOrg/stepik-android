package org.stepik.android.data.announcement.repository

import io.reactivex.Single
import org.stepik.android.data.announcement.source.AnnouncementCacheDataSource
import org.stepik.android.data.announcement.source.AnnouncementRemoteDataSource
import org.stepik.android.data.base.repository.delegate.ListRepositoryDelegate
import org.stepik.android.domain.announcement.model.Announcement
import org.stepik.android.domain.announcement.repository.AnnouncementRepository
import org.stepik.android.domain.base.DataSourceType
import javax.inject.Inject

class AnnouncementRepositoryImpl
@Inject
constructor(
    private val announcementRemoteDataSource: AnnouncementRemoteDataSource,
    private val announcementCacheDataSource: AnnouncementCacheDataSource
) : AnnouncementRepository {
    private val delegate =
        ListRepositoryDelegate(
            announcementRemoteDataSource::getAnnouncements,
            announcementCacheDataSource::getAnnouncements,
            announcementCacheDataSource::saveAnnouncements
        )

    override fun getAnnouncements(announcementIds: List<Long>, sourceType: DataSourceType): Single<List<Announcement>> =
        delegate.get(announcementIds, sourceType, allowFallback = true)
}