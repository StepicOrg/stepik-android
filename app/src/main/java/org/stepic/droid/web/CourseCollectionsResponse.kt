package org.stepic.droid.web

import com.google.gson.annotations.SerializedName
import org.stepic.droid.model.CourseCollection
import org.stepic.droid.model.Meta

class CourseCollectionsResponse(
        meta: Meta,
        @SerializedName("course-lists")
        val courseCollections: List<CourseCollection>
) : StepicResponseBase(meta)
