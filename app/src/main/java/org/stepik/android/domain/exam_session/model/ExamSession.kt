package org.stepik.android.domain.exam_session.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import ru.nobird.android.core.model.Identifiable
import java.util.Date

@Entity
data class ExamSession(
    @PrimaryKey
    @SerializedName("id")
    override val id: Long,
    @SerializedName("user")
    val user: Long,
    @SerializedName("section")
    val section: Long,
    @SerializedName("begin_date")
    val beginDate: Date?,
    @SerializedName("end_date")
    val endDate: Date?,
    @SerializedName("time_left")
    val timeLeft: Float,
    @SerializedName("random_exam")
    val randomExam: Int
) : Identifiable<Long> {
    val isActive: Boolean
        get() = timeLeft > 0
}
