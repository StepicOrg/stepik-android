package org.stepik.android.presentation.course_revenue

import org.stepik.android.domain.course_revenue.model.CourseBenefitByMonthListItem
import ru.nobird.app.core.model.PagedList

interface CourseBenefitsMonthlyFeature {
    sealed class State {
        object Loading : State()
        object Empty : State()
        object Error : State()
        data class Content(
            val courseBenefitByMonthListDataItems: PagedList<CourseBenefitByMonthListItem.Data>,
            val courseBenefitByMonthListItems: List<CourseBenefitByMonthListItem>
        ) : State()
    }

    sealed class Message {
        data class TryAgain(val courseId: Long) : Message()
        data class FetchCourseBenefitsByMonthNext(val courseId: Long) : Message()
        data class FetchCourseBenefitsByMonthsSuccess(val courseBenefitByMonthListDataItems: PagedList<CourseBenefitByMonthListItem.Data>) : Message()
        object FetchCourseBenefitsByMonthsFailure : Message()
    }

    sealed class Action {
        data class FetchCourseBenefitsByMonths(val courseId: Long, val page: Int = 1) : Action()
        sealed class ViewAction : Action()
    }
}