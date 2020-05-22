package org.stepik.android.remote.magic_links.service

import io.reactivex.Single
import org.stepik.android.remote.magic_links.model.MagicLinksRequest
import org.stepik.android.remote.magic_links.model.MagicLinksResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface MagicLinksService {
    @POST("api/magic-links")
    fun createMagicLink(@Body magicLinksRequest: MagicLinksRequest): Single<MagicLinksResponse>
}