package org.stepic.droid.model.comments

import com.google.gson.annotations.SerializedName

enum class VoteValue private constructor(val value: String?) {
    @SerializedName("epic")
    like("epic"),
    @SerializedName("abuse")
    dislike("abuse"),
    remove(null)
}
