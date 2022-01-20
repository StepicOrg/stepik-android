package org.stepik.android.domain.announcement.repository

import io.reactivex.Single
import org.stepik.android.domain.announcement.model.Announcement
import org.stepik.android.domain.base.DataSourceType

interface AnnouncementRepository {
    fun getAnnouncements(announcementIds: List<Long>, sourceType: DataSourceType = DataSourceType.CACHE): Single<List<Announcement>>
}