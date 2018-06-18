package org.stepic.droid.web.achievements

import io.reactivex.Single
import org.stepic.droid.web.achievements.model.AchievementProgressesResponse
import org.stepic.droid.web.achievements.model.AchievementsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface AchievementsService {

    @GET("api/achievements")
    fun getAchivements(
            @Query("ids[]") ids: LongArray? = null,
            @Query("kind") kind: String? = null
    ): Single<AchievementsResponse>

    @GET("api/achievement-progresses")
    fun getAchievementProgresses(
            @Query("ids[]") ids: LongArray? = null,
            @Query("kind") kind: String? = null,
            @Query("achievement") achievement: Long? = null,
            @Query("user") user: Long? = null,
            @Query("is_obtained") isObtained: Boolean? = null,
            @Query("order") order: String? = null
    ): Single<AchievementProgressesResponse>

}