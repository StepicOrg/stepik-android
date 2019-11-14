package org.stepik.android.remote.course_list.model

import com.google.gson.annotations.SerializedName
import org.stepik.android.model.CourseCollection
import org.stepik.android.model.Meta
import org.stepik.android.remote.base.model.MetaResponse

class CourseCollectionsResponse(
    @SerializedName("meta")
    override val meta: Meta,
    @SerializedName("course-lists")
    val courseCollections: List<CourseCollection>
) : MetaResponse