package org.stepik.android.presentation.vote.model

import org.stepik.android.model.comments.Vote

sealed class VoteStatus {
    data class Resolved(val vote: Vote) : VoteStatus()
    object Pending : VoteStatus()
}