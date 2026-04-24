package org.stepik.android.domain.rubricator.model

data class RubricatorData(
    val subjects: List<Subject>,
    val metaCategories: List<MetaCategory>,
    val courseLists: List<CourseList>
) {
    companion object {
        val EMPTY = RubricatorData(
            emptyList(),
            emptyList(),
            emptyList()
        )
    }
}
