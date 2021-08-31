package org.stepik.android.view.submission.routing

import android.net.Uri
import org.stepic.droid.configuration.EndpointResolver
import org.stepik.android.view.base.routing.ExternalDeepLinkProcessor
import javax.inject.Inject

class SubmissionDeepLinkBuilder
@Inject
constructor(
    private val endpointResolver: EndpointResolver,
    private val externalDeepLinkProcessor: ExternalDeepLinkProcessor
) {
    fun createSubmissionLink(stepId: Long, submissionId: Long): String =
        Uri.parse("${endpointResolver.getBaseUrl()}/submissions/$stepId/$submissionId")
            .buildUpon()
            .let(externalDeepLinkProcessor::processExternalDeepLink)
            .build()
            .toString()
}