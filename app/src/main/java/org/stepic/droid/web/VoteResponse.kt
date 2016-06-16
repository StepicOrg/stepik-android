package org.stepic.droid.web

import org.stepic.droid.model.Meta
import org.stepic.droid.model.comments.Vote

class VoteResponse {
    val meta: Meta? = null
    val votes: List<Vote>? = null
    val detail : String? = null //"You do not have permission to perform this action."
}
