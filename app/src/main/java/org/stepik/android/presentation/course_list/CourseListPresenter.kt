package org.stepik.android.presentation.course_list

import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.course_list.interactor.CourseListInteractor
import ru.nobird.android.presentation.base.PresenterBase
import timber.log.Timber
import javax.inject.Inject

class CourseListPresenter
@Inject
constructor(
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler,
    private val interactor: CourseListInteractor
) : PresenterBase<CourseListView>() {

    fun getCourseListItems(vararg ids: Long) {
        compositeDisposable += interactor
            .getCourseListItems(*ids)
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onSuccess = { Timber.d("Success: $it") },
                onError = { Timber.d("Error: $it") }
            )
    }
}