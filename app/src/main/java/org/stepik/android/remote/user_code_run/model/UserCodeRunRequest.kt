package org.stepik.android.remote.user_code_run.model

import com.google.gson.annotations.SerializedName
import org.stepik.android.model.code.UserCodeRun

class UserCodeRunRequest(
    @SerializedName("userCodeRun")
    val userCodeRun: UserCodeRun
)