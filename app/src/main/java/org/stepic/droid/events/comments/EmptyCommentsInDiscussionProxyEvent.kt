package org.stepic.droid.events.comments

import org.stepic.droid.model.comments.DiscussionProxy

data class EmptyCommentsInDiscussionProxyEvent(val discussionProxyId: String, val discussionProxy : DiscussionProxy? = null)