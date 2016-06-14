package org.stepic.droid.model.comments

enum class VoteValue private constructor(val value: String?) {
    like("epic"),
    dislike("abuse"),
    remove(null)
}
