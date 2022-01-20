package org.stepik.android.remote.announcement

import io.reactivex.Single
import org.stepik.android.data.announcement.source.AnnouncementRemoteDataSource
import org.stepik.android.domain.announcement.model.Announcement
import org.stepik.android.remote.announcement.model.AnnouncementResponse
import org.stepik.android.remote.announcement.service.AnnouncementService
import javax.inject.Inject

class AnnouncementRemoteDataSourceImpl
@Inject
constructor(
    private val announcementService: AnnouncementService
) : AnnouncementRemoteDataSource {
    override fun getAnnouncements(announcementIds: List<Long>): Single<List<Announcement>> =
        announcementService
            .getAnnouncements(announcementIds)
            .map(AnnouncementResponse::announcements)
}