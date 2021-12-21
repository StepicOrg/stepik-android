package org.stepik.android.view.injection.course

import android.app.Activity
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import org.stepik.android.domain.course.analytic.CourseViewSource
import org.stepik.android.domain.course_purchase.model.PurchaseResult
import org.stepik.android.presentation.course.CoursePresenter
import org.stepik.android.presentation.course_purchase.model.CoursePurchaseData
import org.stepik.android.view.course.ui.delegates.CourseHeaderDelegate

@AssistedFactory
interface CourseHeaderDelegateFactory {
    fun create(
        courseActivity: Activity,
        coursePresenter: CoursePresenter,
        courseViewSource: CourseViewSource,
        @Assisted("isAuthorized")
        isAuthorized: Boolean,
        @Assisted("mustShowCourseRevenue")
        mustShowCourseRevenue: Boolean,
        @Assisted("showCourseRevenueAction")
        showCourseRevenueAction: () -> Unit,
        @Assisted("onSubmissionCountClicked")
        onSubmissionCountClicked: () -> Unit,
        @Assisted("isLocalSubmissionsEnabled")
        isLocalSubmissionsEnabled: Boolean,
        @Assisted("showCourseSearchAction")
        showCourseSearchAction: () -> Unit,
        coursePurchaseFlowAction: (CoursePurchaseData, PurchaseResult) -> Unit
    ): CourseHeaderDelegate
}