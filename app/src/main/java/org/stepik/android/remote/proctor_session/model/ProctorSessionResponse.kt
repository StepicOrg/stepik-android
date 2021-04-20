package org.stepik.android.remote.proctor_session.model

import com.google.gson.annotations.SerializedName
import org.stepik.android.domain.proctor_session.model.ProctorSession
import org.stepik.android.model.Meta
import org.stepik.android.remote.base.model.MetaResponse

class ProctorSessionResponse(
    @SerializedName("meta")
    override val meta: Meta,
    @SerializedName("proctor-sessions")
    val proctorSessions: List<ProctorSession>
) : MetaResponse