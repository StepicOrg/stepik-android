package org.stepik.android.data.magic_links.repository

import io.reactivex.Single
import org.stepik.android.data.magic_links.source.MagicLinksRemoteDataSource
import org.stepik.android.domain.magic_links.model.MagicLink
import org.stepik.android.domain.magic_links.repository.MagicLinksRepository
import javax.inject.Inject

class MagicLinksRepositoryImpl
@Inject
constructor(
    private val magicLinksRemoteDataSource: MagicLinksRemoteDataSource
) : MagicLinksRepository {
    override fun createMagicLink(url: String): Single<MagicLink> =
        magicLinksRemoteDataSource.createMagicLink(url)
}