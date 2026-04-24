package org.stepik.android.presentation.rubricator

import org.stepik.android.domain.rubricator.model.CourseList
import org.stepik.android.domain.rubricator.model.MetaCategory
import org.stepik.android.domain.rubricator.model.RubricatorData
import org.stepik.android.domain.rubricator.model.Subject

interface RubricatorFeature {
    data class State(
        val rubricatorState: RubricatorState,
        val metaCategoryState: MetaCategoryState,
        val courseListState: CourseListState
    )

    /**
     * States
     */

    sealed class RubricatorState {
        object Idle : RubricatorState()
        object Loading : RubricatorState()
        object Error : RubricatorState()
        data class Content(val rubricatorData: RubricatorData) : RubricatorState()
    }

    sealed class MetaCategoryState {
        object Empty : MetaCategoryState()
        data class Content(val subjectTitle: String, val metaCategories: List<MetaCategory>) : MetaCategoryState()
    }

    sealed class CourseListState {
        object Empty : CourseListState()
        data class Content(val metaCategoryTitle: String, val courseLists: List<CourseList>) : CourseListState()
    }

    /**
     * Messages
     */

    sealed class Message {
        data class InitMessage(val rubricatorUrl: String) : Message()
        data class FetchRubricatorDataSuccess(val rubricatorData: RubricatorData) : Message()
        object FetchRubricatorDataError : Message()

        data class FetchMetaCategories(val subject: Subject) : Message()

        data class FetchCourseLists(val metaCategory: MetaCategory) : Message()

        object OnBackPressed : Message()
    }

    /**
     * Actions
     */

    sealed class Action {
        data class FetchRubricatorData(val rubricatorUrl: String) : Action()

        sealed class ViewAction : Action() {
            object CloseScreen : ViewAction()
        }
    }
}