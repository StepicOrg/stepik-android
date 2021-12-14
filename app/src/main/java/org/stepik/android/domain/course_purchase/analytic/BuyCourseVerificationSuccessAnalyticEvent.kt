package org.stepik.android.domain.course_purchase.analytic

import org.stepik.android.domain.base.analytic.AnalyticEvent
import ru.nobird.android.core.model.mapOfNotNull

class BuyCourseVerificationSuccessAnalyticEvent(
    courseId: Long,
    coursePurchaseSource: String,
    isWishlisted: Boolean,
    promoName: String?
) : AnalyticEvent {
    companion object {
        private const val PARAM_COURSE = "course"
        private const val PARAM_SOURCE = "source"
        private const val PARAM_IS_WISHLISTED = "is_wishlisted"
        private const val PARAM_PROMO = "promo"
    }
    override val name: String =
        "Buy course verification success"

    override val params: Map<String, Any> =
        mapOfNotNull(
            PARAM_COURSE to courseId,
            PARAM_SOURCE to coursePurchaseSource,
            PARAM_IS_WISHLISTED to isWishlisted,
            PARAM_PROMO to promoName
        )
}