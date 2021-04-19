package org.stepik.android.remote.exam_session.model

import com.google.gson.annotations.SerializedName
import org.stepik.android.domain.exam_session.model.ExamSession
import org.stepik.android.model.Meta
import org.stepik.android.remote.base.model.MetaResponse

class ExamSessionResponse(
    @SerializedName("meta")
    override val meta: Meta,
    @SerializedName("exam-sessions")
    val examSessions: List<ExamSession>
) : MetaResponse