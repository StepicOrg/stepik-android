package org.stepik.android.model

import com.google.gson.annotations.SerializedName
import org.stepik.android.model.Progressable
import java.util.*

class Assignment(
    val id: Long,
    val step: Long,
    val unit: Long,
    override val progress: String?,

    @SerializedName("create_date")
    val createDate: Date?,
    @SerializedName("update_date")
    val updateDate: Date?
): Progressable