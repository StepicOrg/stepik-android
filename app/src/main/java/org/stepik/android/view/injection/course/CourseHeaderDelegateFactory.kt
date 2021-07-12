package org.stepik.android.view.injection.course

import android.app.Activity
import dagger.assisted.AssistedFactory
import org.stepik.android.domain.course.analytic.CourseViewSource
import org.stepik.android.presentation.course.CoursePresenter
import org.stepik.android.view.course.ui.delegates.CourseHeaderDelegate

@AssistedFactory
interface CourseHeaderDelegateFactory {
    fun create(
        courseActivity: Activity,
        coursePresenter: CoursePresenter,
        courseViewSource: CourseViewSource,
        isAuthorized: Boolean,
        mustShowCourseBenefits: Boolean,
        showCourseBenefitsAction: () -> Unit,
        onSubmissionCountClicked: () -> Unit,
        isLocalSubmissionsEnabled: Boolean
    ): CourseHeaderDelegate
}