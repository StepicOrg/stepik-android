package org.stepik.android.view.step.routing

import android.net.Uri
import org.stepic.droid.configuration.EndpointResolver
import org.stepik.android.model.Step
import org.stepik.android.model.comments.DiscussionThread
import org.stepik.android.view.base.routing.ExternalDeepLinkProcessor
import javax.inject.Inject

class StepDeepLinkBuilder
@Inject
constructor(
    private val endpointResolver: EndpointResolver,
    private val externalDeepLinkProcessor: ExternalDeepLinkProcessor
) {
    companion object {
        private const val PARAM_DISCUSSION_ID = "discussion"
        private const val PARAM_DISCUSSION_THREAD = "thread"
    }

    fun createStepLink(
        step: Step,
        discussionThread: DiscussionThread? = null,
        discussionId: Long? = null
    ): String {
        val uri = Uri
            .parse("${endpointResolver.getBaseUrl()}/lesson/${step.lesson}/step/${step.position}")
            .buildUpon()
            .let(externalDeepLinkProcessor::processExternalDeepLink)

        if (discussionThread != null) {
            uri.appendQueryParameter(PARAM_DISCUSSION_THREAD, discussionThread.thread)
        }

        if (discussionId != null) {
            uri.appendQueryParameter(PARAM_DISCUSSION_ID, discussionId.toString())
        }
        return uri.build().toString()
    }
}