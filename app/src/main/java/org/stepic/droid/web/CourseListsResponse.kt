package org.stepic.droid.web

import com.google.gson.annotations.SerializedName
import org.stepic.droid.model.CourseListItem
import org.stepic.droid.model.Meta

class CourseListsResponse(
        meta: Meta,
        @SerializedName("course-lists")
        val courseLists: List<CourseListItem>
) : MetaResponseBase(meta)
