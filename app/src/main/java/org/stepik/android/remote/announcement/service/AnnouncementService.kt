package org.stepik.android.remote.announcement.service

import io.reactivex.Single
import org.stepik.android.remote.announcement.model.AnnouncementResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface AnnouncementService {
    @GET("api/announcements")
    fun getAnnouncements(@Query("ids[]") announcementIds: List<Long>): Single<AnnouncementResponse>
}