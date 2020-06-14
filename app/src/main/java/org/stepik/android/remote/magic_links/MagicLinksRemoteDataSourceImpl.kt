package org.stepik.android.remote.magic_links

import io.reactivex.Single
import org.stepik.android.data.magic_links.source.MagicLinksRemoteDataSource
import org.stepik.android.domain.magic_links.model.MagicLink
import org.stepik.android.remote.magic_links.model.MagicLinksRequest
import org.stepik.android.remote.magic_links.service.MagicLinksService
import javax.inject.Inject

class MagicLinksRemoteDataSourceImpl
@Inject
constructor(
    private val magicLinksService: MagicLinksService
) : MagicLinksRemoteDataSource {
    override fun createMagicLink(url: String): Single<MagicLink> =
        magicLinksService
            .createMagicLink(MagicLinksRequest(MagicLinksRequest.Body(url)))
            .map { it.magicLinks.first() }
}