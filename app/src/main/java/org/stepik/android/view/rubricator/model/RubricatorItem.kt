package org.stepik.android.view.rubricator.model

import org.stepik.android.domain.rubricator.model.CourseList
import org.stepik.android.domain.rubricator.model.MetaCategory
import org.stepik.android.domain.rubricator.model.Subject
import ru.nobird.app.core.model.Identifiable

sealed class RubricatorItem {
    data class SubjectItem(val subject: Subject) : RubricatorItem(), Identifiable<String> {
        override val id: String = "subject_${subject.id}"
    }

    data class MetaCategoryItem(val metaCategory: MetaCategory) : RubricatorItem(), Identifiable<String> {
        override val id: String = "meta_category_${metaCategory.id}"
    }

    data class CourseListItem(val courseList: CourseList) : RubricatorItem(), Identifiable<String> {
        override val id: String = "course_list_${courseList.id}"
    }
}