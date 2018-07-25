package org.stepic.droid.core.presenters.contracts

import org.stepik.android.model.comments.DiscussionProxy

interface DiscussionView {

    fun onInternetProblemInComments()

    fun onEmptyComments(discussionProxy: DiscussionProxy)

    fun onLoaded(discussionProxy: DiscussionProxy)
}
