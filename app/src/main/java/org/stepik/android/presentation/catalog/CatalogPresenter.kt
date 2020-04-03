package org.stepik.android.presentation.catalog

import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.catalog.interactor.CatalogInteractor
import org.stepik.android.model.CourseCollection
import org.stepik.android.presentation.course_list.CourseListCollectionPresenter
import ru.nobird.android.presentation.base.PresenterBase
import javax.inject.Inject
import javax.inject.Provider

class CatalogPresenter
@Inject
constructor(
    // TODO Inject Filters and Tags
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler,
    private val catalogInteractor: CatalogInteractor,
    private val courseListCollectionPresenterProvider: Provider<CourseListCollectionPresenter>
) : PresenterBase<CatalogView>() {
    fun fetchCollections() {
        compositeDisposable += catalogInteractor
            .fetchCourseCollections()
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onSuccess = { courseCollections ->
                    // TODO Add Filters and Tags Presenter
                    courseCollections.map {
                        courseListCollectionPresenterProvider.get().apply { fetchCourses(*it.courses) }
                    }
                },
                onError = {}
            )
    }
}