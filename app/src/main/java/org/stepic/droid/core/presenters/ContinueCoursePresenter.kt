package org.stepic.droid.core.presenters

import org.stepic.droid.core.presenters.contracts.ContinueCourseView
import org.stepik.android.presentation.course_continue.delegate.CourseContinuePresenterDelegate
import ru.nobird.android.presentation.base.PresenterBase
import ru.nobird.android.presentation.base.PresenterViewContainer
import javax.inject.Inject

class ContinueCoursePresenter
@Inject
constructor(
    viewContainer: PresenterViewContainer<ContinueCourseView>,
    private val continueCoursePresenterDelegate: CourseContinuePresenterDelegate
) : PresenterBase<ContinueCourseView>(viewContainer), CourseContinuePresenterDelegate by continueCoursePresenterDelegate
