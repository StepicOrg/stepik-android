package org.stepik.android.data.announcement.source

import io.reactivex.Single
import org.stepik.android.domain.announcement.model.Announcement

interface AnnouncementRemoteDataSource {
    fun getAnnouncements(announcementIds: List<Long>): Single<List<Announcement>>
}