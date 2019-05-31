package org.stepik.android.remote.assignment.model

import com.google.gson.annotations.SerializedName
import org.stepik.android.model.Assignment
import org.stepik.android.model.Meta
import org.stepik.android.remote.base.model.MetaResponse

class AssignmentResponse(
    @SerializedName("meta")
    override val meta: Meta,
    @SerializedName("assignments")
    val assignments: List<Assignment>
) : MetaResponse
