package org.stepik.android.model

import com.google.gson.annotations.SerializedName
import java.util.*

class Assignment(
    @SerializedName("id")
    val id: Long,
    @SerializedName("step")
    val step: Long,
    @SerializedName("unit")
    val unit: Long,
    @SerializedName("progress")
    override val progress: String?,

    @SerializedName("create_date")
    val createDate: Date?,
    @SerializedName("update_date")
    val updateDate: Date?
) : Progressable