package org.stepik.android.remote.achievement.service

import io.reactivex.Observable
import io.reactivex.Single
import org.stepik.android.remote.achievement.model.AchievementProgressesResponse
import org.stepik.android.remote.achievement.model.AchievementsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface AchievementsService {
    @GET("api/achievements")
    fun getAchievements(
        @Query("ids[]") ids: LongArray? = null,
        @Query("kind") kind: String? = null,
        @Query("page") page: Int? = null
    ): Observable<AchievementsResponse>

    @GET("api/achievement-progresses")
    fun getAchievementProgresses(
        @Query("ids[]") ids: LongArray? = null,
        @Query("kind") kind: String? = null,
        @Query("achievement") achievement: Long? = null,
        @Query("user") user: Long? = null,
        @Query("is_obtained") isObtained: Boolean? = null,
        @Query("order") order: String? = null,
        @Query("page") page: Int? = null
    ): Single<AchievementProgressesResponse>
}