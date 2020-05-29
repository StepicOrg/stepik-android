package org.stepik.android.domain.magic_links.repository

import io.reactivex.Single
import org.stepik.android.domain.magic_links.model.MagicLink

interface MagicLinksRepository {
    fun createMagicLink(url: String): Single<MagicLink>
}