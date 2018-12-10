package org.stepik.android.presentation.course_content

import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.course_content.interactor.CourseContentInteractor
import org.stepik.android.presentation.base.PresenterBase
import javax.inject.Inject

class CourseContentPresenter
@Inject
constructor(
    private val courseContentInteractor: CourseContentInteractor,

    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : PresenterBase<CourseContentView>() {

    override fun attachView(view: CourseContentView) {
        super.attachView(view)

        compositeDisposable += courseContentInteractor
            .getCourseContent()
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onNext  = { view.setCourseContent(it) },
                onError = { it.printStackTrace() }
            )
    }
}