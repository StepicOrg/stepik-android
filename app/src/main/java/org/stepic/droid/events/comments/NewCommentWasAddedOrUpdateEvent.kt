package org.stepic.droid.events.comments

import org.stepic.droid.model.comments.Comment

data class NewCommentWasAddedOrUpdateEvent(val targetId : Long, val newCommentInsertOrUpdate: Comment?)
