package org.stepik.android.domain.magic_links.interactor

import io.reactivex.Single
import org.stepic.droid.configuration.Config
import org.stepik.android.domain.magic_links.model.MagicLink
import org.stepik.android.domain.magic_links.repository.MagicLinksRepository
import javax.inject.Inject

class MagicLinkInteractor
@Inject
constructor(
    private val config: Config,
    private val magicLinksRepository: MagicLinksRepository
) {
    /**
     * Creates magic link for given absolute or relative [url] on stepik domain
     */
    fun createMagicLink(url: String): Single<MagicLink> =
        magicLinksRepository
            .createMagicLink(url.removePrefix(config.baseUrl))
}