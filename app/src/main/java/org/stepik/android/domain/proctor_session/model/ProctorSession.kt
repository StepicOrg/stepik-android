package org.stepik.android.domain.proctor_session.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import ru.nobird.android.core.model.Identifiable
import java.util.Date

@Entity
data class ProctorSession(
    @PrimaryKey
    @SerializedName("id")
    override val id: Long,
    @SerializedName("user")
    val user: Long,
    @SerializedName("section")
    val section: Long,
    @SerializedName("create_date")
    val createDate: Date?,
    @SerializedName("start_url")
    val startUrl: String,
    @SerializedName("stop_url")
    val stopUrl: String,
    @SerializedName("start_date")
    val startDate: Date?,
    @SerializedName("stop_date")
    val stopDate: Date?,
    @SerializedName("submit_date")
    val submitDate: Date?,
    @SerializedName("comment")
    val comment: String,
    @SerializedName("score")
    val score: Float
) : Identifiable<Long>
