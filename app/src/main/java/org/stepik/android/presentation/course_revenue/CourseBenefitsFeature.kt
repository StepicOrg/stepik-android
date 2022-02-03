package org.stepik.android.presentation.course_revenue

import org.stepik.android.domain.course_revenue.model.CourseBeneficiary
import org.stepik.android.domain.course_revenue.model.CourseBenefitListItem
import ru.nobird.app.core.model.PagedList

interface CourseBenefitsFeature {
    sealed class State {
        object Loading : State()
        object Empty : State()
        object Error : State()
        data class Content(
            val courseBenefitListDataItems: PagedList<CourseBenefitListItem.Data>,
            val courseBenefitListItems: List<CourseBenefitListItem>,
            val courseBeneficiary: CourseBeneficiary
        ) : State()
    }

    sealed class Message {
        data class TryAgain(val courseId: Long) : Message()
        data class FetchNextPage(val courseId: Long) : Message()
        data class FetchCourseBenefitsSuccess(
            val courseBenefitListDataItems: PagedList<CourseBenefitListItem.Data>,
            val courseBeneficiary: CourseBeneficiary
        ) : Message()
        data class FetchCourseBenefitsNextSuccess(val courseBenefitListDataItems: PagedList<CourseBenefitListItem.Data>) : Message()
        object FetchCourseBenefitsFailure : Message()
        object FetchCourseBenefitsNextFailure : Message()
    }

    sealed class Action {
        data class FetchCourseBenefits(val courseId: Long) : Action()
        data class FetchCourseBenefitsNext(val courseId: Long, val page: Int) : Action()
        sealed class ViewAction : Action()
    }
}