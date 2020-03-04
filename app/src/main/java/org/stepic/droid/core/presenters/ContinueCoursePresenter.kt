package org.stepic.droid.core.presenters

import org.stepic.droid.core.presenters.contracts.ContinueCourseView
import org.stepik.android.presentation.course_continue.delegate.CourseContinuePresenterDelegate
import org.stepik.android.presentation.course_continue.delegate.CourseContinuePresenterDelegateImpl
import ru.nobird.android.presentation.base.PresenterBase
import ru.nobird.android.presentation.base.PresenterViewContainer
import ru.nobird.android.presentation.base.delegate.PresenterDelegate
import javax.inject.Inject

class ContinueCoursePresenter
@Inject
constructor(
    viewContainer: PresenterViewContainer<ContinueCourseView>,
    private val continueCoursePresenterDelegate: CourseContinuePresenterDelegateImpl
) : PresenterBase<ContinueCourseView>(viewContainer), CourseContinuePresenterDelegate by continueCoursePresenterDelegate {
    override val delegates: List<PresenterDelegate<in ContinueCourseView>> =
        listOf(continueCoursePresenterDelegate)
}
