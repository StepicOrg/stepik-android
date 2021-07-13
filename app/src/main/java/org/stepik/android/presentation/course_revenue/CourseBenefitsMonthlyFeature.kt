package org.stepik.android.presentation.course_revenue

import org.stepik.android.domain.course_revenue.model.CourseBenefitByMonthListItem
import ru.nobird.android.core.model.PagedList

interface CourseBenefitsMonthlyFeature {
    sealed class State {
        object Loading : State()
        object Empty : State()
        object Error : State()
        data class Content(val courseBenefitByMonthListItems: PagedList<CourseBenefitByMonthListItem.Data>) : State()
    }

    sealed class Message {
        data class FetchCourseBenefitsByMonthsSuccess(val courseBenefitByMonthListItems: PagedList<CourseBenefitByMonthListItem.Data>) : Message()
        object FetchCourseBenefitsByMonthsFailure : Message()
    }

    sealed class Action {
        data class FetchCourseBenefitsByMonths(val courseId: Long) : Action()
        sealed class ViewAction : Action()
    }
}