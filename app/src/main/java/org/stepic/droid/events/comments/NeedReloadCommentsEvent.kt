package org.stepic.droid.events.comments

import org.stepic.droid.model.comments.Comment

data class NeedReloadCommentsEvent (val targetId : Long, val newCommentInsert: Comment?)
