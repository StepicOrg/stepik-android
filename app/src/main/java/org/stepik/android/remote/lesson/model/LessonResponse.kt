package org.stepik.android.remote.lesson.model

import com.google.gson.annotations.SerializedName
import org.stepik.android.model.Lesson
import org.stepik.android.model.Meta
import org.stepik.android.remote.base.model.MetaResponse

class LessonResponse(
    @SerializedName("meta")
    override val meta: Meta,
    @SerializedName("lessons")
    val lessons: List<Lesson>
) : MetaResponse
