package org.stepik.android.view.submission.routing

import android.net.Uri
import org.stepic.droid.configuration.Config
import org.stepik.android.view.base.routing.ExternalDeepLinkProcessor
import javax.inject.Inject

class SubmissionDeepLinkBuilder
@Inject
constructor(
    private val config: Config,
    private val externalDeepLinkProcessor: ExternalDeepLinkProcessor
) {
    fun createSubmissionLink(stepId: Long, submissionId: Long): String =
        Uri.parse("${config.baseUrl}/submissions/$stepId/$submissionId")
            .buildUpon()
            .let(externalDeepLinkProcessor::processExternalDeepLink)
            .build()
            .toString()
}