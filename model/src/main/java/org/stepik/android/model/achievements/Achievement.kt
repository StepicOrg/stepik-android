package org.stepik.android.model.achievements

import com.google.gson.annotations.SerializedName

class Achievement(
    val id: Long,
    val kind: String,

    @SerializedName("target_score")
    val targetScore: Int
)