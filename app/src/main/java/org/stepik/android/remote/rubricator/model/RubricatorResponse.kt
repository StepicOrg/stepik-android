package org.stepik.android.remote.rubricator.model

import com.google.gson.annotations.SerializedName
import org.stepik.android.domain.rubricator.model.CourseList
import org.stepik.android.domain.rubricator.model.MetaCategory
import org.stepik.android.domain.rubricator.model.Subject

class RubricatorResponse(
    @SerializedName("subjects")
    val subjects: List<Subject>,
    @SerializedName("meta_categories")
    val metaCategories: List<MetaCategory>,
    @SerializedName("course_lists")
    val courseLists: List<CourseList>
)