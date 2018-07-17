package org.stepic.droid.web

import org.stepic.droid.model.comments.Vote
import org.stepik.android.model.Meta

class VoteResponse {
    val meta: Meta? = null
    val votes: List<Vote>? = null
    val detail : String? = null //"You do not have permission to perform this action."
}
