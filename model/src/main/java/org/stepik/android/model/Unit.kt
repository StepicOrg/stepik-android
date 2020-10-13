package org.stepik.android.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import ru.nobird.android.core.model.Identifiable
import java.util.Date

@Parcelize
class Unit(
    @SerializedName("id")
    override val id: Long = 0,
    @SerializedName("section")
    val section: Long = 0,
    @SerializedName("lesson")
    val lesson: Long = 0,
    @SerializedName("assignments")
    val assignments: List<Long>? = null,
    @SerializedName("position")
    val position: Int = 0,
    @SerializedName("progress")
    override val progress: String? = null,

    @SerializedName("begin_date")
    val beginDate: Date? = null,
    @SerializedName("end_date")
    val endDate: Date? = null,
    @SerializedName("soft_deadline")
    val softDeadline: Date? = null,
    @SerializedName("hard_deadline")
    val hardDeadline: Date? = null,

    @SerializedName("grading_policy")
    val gradingPolicy: String? = null,

    @SerializedName("begin_date_source")
    val beginDateSource: String? = null,
    @SerializedName("end_date_source")
    val endDateSource: String? = null,
    @SerializedName("soft_deadline_source")
    val softDeadlineSource: String? = null,
    @SerializedName("hard_deadline_source")
    val hardDeadlineSource: String? = null,
    @SerializedName("grading_policy_source")
    val gradingPolicySource: String? = null,

    @SerializedName("is_active")
    val isActive: Boolean = false,

    @SerializedName("create_date")
    val createDate: Date? = null,
    @SerializedName("update_date")
    val updateDate: Date? = null
) : Parcelable, Progressable, Identifiable<Long>
