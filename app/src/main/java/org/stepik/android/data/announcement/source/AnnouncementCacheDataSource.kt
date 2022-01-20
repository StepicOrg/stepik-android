package org.stepik.android.data.announcement.source

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.domain.announcement.model.Announcement

interface AnnouncementCacheDataSource {
    fun getAnnouncements(announcementIds: List<Long>): Single<List<Announcement>>
    fun saveAnnouncements(announcements: List<Announcement>): Completable
}