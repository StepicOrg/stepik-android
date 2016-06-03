package org.stepic.droid.events.comments

import org.stepic.droid.model.comments.Comment

data class NewCommentWasAdded(val targetId : Long, val newCommentInsert: Comment?)
