package org.stepic.droid.core.presenters

import org.stepic.droid.core.presenters.contracts.ContinueCourseView
import org.stepik.android.presentation.course_continue.delegate.ContinueCoursePresenterDelegate
import ru.nobird.android.presentation.base.PresenterBase
import ru.nobird.android.presentation.base.PresenterViewContainer
import javax.inject.Inject

class ContinueCoursePresenter
@Inject
constructor(
    viewContainer: PresenterViewContainer<ContinueCourseView>,
    private val continueCoursePresenterDelegate: ContinueCoursePresenterDelegate
) : PresenterBase<ContinueCourseView>(viewContainer), ContinueCoursePresenterDelegate by continueCoursePresenterDelegate
