package org.stepik.android.domain.banner.analytic

import org.stepik.android.domain.banner.model.Banner
import org.stepik.android.domain.base.analytic.AnalyticEvent
import ru.nobird.app.core.model.mapOfNotNull

class PromoBannerClickedAnalyticEvent(banner: Banner) : AnalyticEvent {
    companion object {
        private const val PARAM_TYPE = "type"
        private const val PARAM_LANG = "lang"
        private const val PARAM_TITLE = "title"
        private const val PARAM_DESCRIPTION = "description"
        private const val PARAM_URL = "url"
        private const val PARAM_SCREEN = "screen"
        private const val PARAM_POSITION = "position"
    }

    override val name: String
        get() = "Promo banner clicked"

    override val params: Map<String, Any> =
        mapOfNotNull(
            PARAM_TYPE to banner.type?.name?.lowercase(),
            PARAM_LANG to banner.language,
            PARAM_TITLE to banner.title,
            PARAM_DESCRIPTION to banner.description,
            PARAM_URL to banner.url,
            PARAM_SCREEN to banner.screen,
            PARAM_POSITION to banner.position
        )
}
