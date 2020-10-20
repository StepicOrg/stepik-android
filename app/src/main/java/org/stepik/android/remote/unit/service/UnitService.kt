package org.stepik.android.remote.unit.service

import io.reactivex.Single
import org.stepik.android.remote.unit.model.UnitResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface UnitService {
    @GET("api/units")
    fun getUnits(@Query("ids[]") unitIds: List<Long>): Single<UnitResponse>

    @GET("api/units")
    fun getUnitsByLessonId(@Query("lesson") lessonId: Long): Single<UnitResponse>

    @GET("api/units")
    fun getUnits(
        @Query("course") courseId: Long,
        @Query("lesson") lessonId: Long
    ): Single<UnitResponse>
}