package org.stepik.android.data.magic_links.source

import io.reactivex.Single
import org.stepik.android.domain.magic_links.model.MagicLink

interface MagicLinksRemoteDataSource {
    fun createMagicLink(url: String): Single<MagicLink>
}