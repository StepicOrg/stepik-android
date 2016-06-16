package org.stepic.droid.events.comments

import org.stepic.droid.model.comments.Vote

class LikeCommentSuccessEvent (val commentId : Long, val vote : Vote){
}