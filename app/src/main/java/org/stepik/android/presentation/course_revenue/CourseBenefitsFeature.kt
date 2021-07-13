package org.stepik.android.presentation.course_revenue

import org.stepik.android.domain.course_revenue.model.CourseBeneficiary
import org.stepik.android.domain.course_revenue.model.CourseBenefitListItem
import ru.nobird.android.core.model.PagedList

interface CourseBenefitsFeature {
    sealed class State {
        object Loading : State()
        object Empty : State()
        object Error : State()
        data class Content(
            val courseBenefitListItems: PagedList<CourseBenefitListItem.Data>,
            val courseBeneficiary: CourseBeneficiary
        ) : State()
    }

    sealed class Message {
        data class FetchCourseBenefitsSuccess(
            val courseBenefitListItems: PagedList<CourseBenefitListItem.Data>,
            val courseBeneficiary: CourseBeneficiary
        ) : Message()
        object FetchCourseBenefitsFailure : Message()
    }

    sealed class Action {
        data class FetchCourseBenefits(val courseId: Long) : Action()
        sealed class ViewAction : Action()
    }
}