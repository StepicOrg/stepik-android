package org.stepik.android.remote.user.model

import com.google.gson.annotations.SerializedName
import org.stepik.android.model.user.User
import org.stepik.android.model.Meta
import org.stepik.android.remote.base.model.MetaResponse

class UserResponse(
    @SerializedName("meta")
    override val meta: Meta,
    @SerializedName("users")
    val users: List<User>
) : MetaResponse
