package org.stepic.droid.core.presenters.contracts

import org.stepic.droid.model.comments.DiscussionProxy

interface DiscussionView {

    fun onInternetProblemInComments()

    fun onEmptyComments(discussionProxy: DiscussionProxy)

    fun onLoaded(discussionProxy: DiscussionProxy)
}
