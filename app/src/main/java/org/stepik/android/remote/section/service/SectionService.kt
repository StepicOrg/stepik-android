package org.stepik.android.remote.section.service

import io.reactivex.Single
import org.stepik.android.remote.section.model.SectionResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface SectionService {
    @GET("api/sections")
    fun getSections(@Query("ids[]") sectionIds: LongArray): Single<SectionResponse>
}