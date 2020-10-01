package org.stepik.android.view.step_quiz_review.routing

import android.net.Uri
import org.stepic.droid.configuration.Config
import org.stepik.android.view.base.routing.ExternalDeepLinkProcessor
import javax.inject.Inject

class StepQuizReviewDeepLinkBuilder
@Inject
constructor(
    private val config: Config,
    private val externalDeepLinkProcessor: ExternalDeepLinkProcessor
) {
    fun createTakenReviewDeepLink(sessionId: Long): String =
        Uri.parse("${config.baseUrl}/review/sessions/$sessionId")
            .buildUpon()
            .let(externalDeepLinkProcessor::processExternalDeepLink)
            .build()
            .toString()

    fun createReviewDeepLink(reviewId: Long): String =
        Uri.parse("${config.baseUrl}/review/reviews/$reviewId")
            .buildUpon()
            .let(externalDeepLinkProcessor::processExternalDeepLink)
            .build()
            .toString()
}