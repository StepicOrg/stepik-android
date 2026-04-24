package org.stepik.android.remote.rubricator.service

import io.reactivex.Single
import org.stepik.android.remote.rubricator.model.RubricatorResponse
import retrofit2.http.GET
import retrofit2.http.Url

interface RubricatorService {
    @GET
    fun getRubricatorData(@Url url: String): Single<RubricatorResponse>
}