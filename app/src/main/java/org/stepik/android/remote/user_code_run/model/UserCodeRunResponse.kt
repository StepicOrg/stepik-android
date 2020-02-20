package org.stepik.android.remote.user_code_run.model

import com.google.gson.annotations.SerializedName
import org.stepik.android.model.Meta
import org.stepik.android.model.code.UserCodeRun
import org.stepik.android.remote.base.model.MetaResponse

class UserCodeRunResponse(
    @SerializedName("meta")
    override val meta: Meta,
    @SerializedName("user-code-runs")
    val userCodeRuns: List<UserCodeRun>
) : MetaResponse