package org.stepik.android.presentation.course_info

import io.reactivex.Scheduler
import io.reactivex.subjects.BehaviorSubject
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.model.Course
import org.stepik.android.presentation.base.PresenterBase
import javax.inject.Inject

class CourseInfoPresenter
@Inject
constructor(
    private val courseSource: BehaviorSubject<Course>,

    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : PresenterBase<CourseInfoView>() {

    init {

    }

}