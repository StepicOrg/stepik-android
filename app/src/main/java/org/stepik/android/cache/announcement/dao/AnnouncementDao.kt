package org.stepik.android.cache.announcement.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.domain.announcement.model.Announcement

@Dao
interface AnnouncementDao {
    @Query("SELECT * FROM Announcement WHERE id IN (:announcementIds)")
    fun getAnnouncement(announcementIds: List<Long>): Single<List<Announcement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveAnnouncements(announcements: List<Announcement>): Completable
}