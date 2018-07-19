package org.stepic.droid.web

import com.google.gson.annotations.SerializedName
import org.stepik.android.model.CourseCollection
import org.stepik.android.model.Meta

class CourseCollectionsResponse(
        meta: Meta,
        @SerializedName("course-lists")
        val courseCollections: List<CourseCollection>
) : MetaResponseBase(meta)