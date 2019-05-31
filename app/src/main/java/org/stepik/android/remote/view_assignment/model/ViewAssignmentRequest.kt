package org.stepik.android.remote.view_assignment.model

import com.google.gson.annotations.SerializedName
import org.stepik.android.model.ViewAssignment

class ViewAssignmentRequest(
    @SerializedName("view")
    val view: ViewAssignment
)
