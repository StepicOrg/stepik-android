package org.stepik.android.view.injection.course_revenue

import androidx.lifecycle.ViewModel
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import org.stepik.android.presentation.base.injection.ViewModelKey
import org.stepik.android.presentation.course_revenue.CourseBenefitSummaryFeature
import org.stepik.android.presentation.course_revenue.CourseRevenueFeature
import org.stepik.android.presentation.course_revenue.CourseBenefitsFeature
import org.stepik.android.presentation.course_revenue.CourseBenefitsMonthlyFeature
import org.stepik.android.presentation.course_revenue.CourseRevenueViewModel
import org.stepik.android.presentation.course_revenue.dispatcher.CourseBenefitSummaryActionDispatcher
import org.stepik.android.presentation.course_revenue.dispatcher.CourseBenefitsActionDispatcher
import org.stepik.android.presentation.course_revenue.dispatcher.CourseBenefitsMonthlyActionDispatcher
import org.stepik.android.presentation.course_revenue.dispatcher.CourseRevenueActionDispatcher
import org.stepik.android.presentation.course_revenue.reducer.CourseRevenueReducer
import ru.nobird.app.core.model.safeCast
import ru.nobird.app.presentation.redux.container.wrapWithViewContainer
import ru.nobird.app.presentation.redux.dispatcher.transform
import ru.nobird.app.presentation.redux.dispatcher.wrapWithActionDispatcher
import ru.nobird.app.presentation.redux.feature.ReduxFeature

@Module
object CourseRevenuePresentationModule {
    /**
     * Presentation
     */
    @Provides
    @IntoMap
    @ViewModelKey(CourseRevenueViewModel::class)
    internal fun provideCourseBenefitsPresenter(
        courseRevenueReducer: CourseRevenueReducer,
        courseRevenueActionDispatcher: CourseRevenueActionDispatcher,
        courseBenefitSummaryActionDispatcher: CourseBenefitSummaryActionDispatcher,
        courseBenefitsActionDispatcher: CourseBenefitsActionDispatcher,
        courseBenefitsMonthlyActionDispatcher: CourseBenefitsMonthlyActionDispatcher
    ): ViewModel =
        CourseRevenueViewModel(
            ReduxFeature(CourseRevenueFeature.State(
                courseRevenueState = CourseRevenueFeature.CourseRevenueState.Idle,
                courseBenefitSummaryState = CourseBenefitSummaryFeature.State.Loading,
                courseBenefitsState = CourseBenefitsFeature.State.Loading,
                courseBenefitsMonthlyState = CourseBenefitsMonthlyFeature.State.Loading
            ), courseRevenueReducer)
                .wrapWithActionDispatcher(courseRevenueActionDispatcher)
                .wrapWithActionDispatcher(
                    courseBenefitSummaryActionDispatcher.transform(
                        transformAction = { it.safeCast<CourseRevenueFeature.Action.CourseBenefitSummaryAction>()?.action },
                        transformMessage = CourseRevenueFeature.Message::CourseBenefitSummaryMessage
                    )
                )
                .wrapWithActionDispatcher(
                    courseBenefitsActionDispatcher.transform(
                        transformAction = { it.safeCast<CourseRevenueFeature.Action.CourseBenefitsAction>()?.action },
                        transformMessage = CourseRevenueFeature.Message::CourseBenefitsMessage
                    )
                )
                .wrapWithActionDispatcher(
                    courseBenefitsMonthlyActionDispatcher.transform(
                        transformAction = { it.safeCast<CourseRevenueFeature.Action.CourseBenefitsMonthlyAction>()?.action },
                        transformMessage = CourseRevenueFeature.Message::CourseBenefitsMonthlyMessage
                    )
                )
                .wrapWithViewContainer()
        )
}