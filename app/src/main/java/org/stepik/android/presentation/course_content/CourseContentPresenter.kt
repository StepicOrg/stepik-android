package org.stepik.android.presentation.course_content

import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.persistence.downloads.progress.DownloadProgressProvider
import org.stepik.android.domain.course_content.interactor.CourseContentInteractor
import org.stepik.android.model.Section
import org.stepik.android.model.Unit
import org.stepik.android.presentation.base.PresenterBase
import org.stepik.android.view.course_content.model.CourseContentItem
import javax.inject.Inject

class CourseContentPresenter
@Inject
constructor(
    private val courseContentInteractor: CourseContentInteractor,

    private val sectionDownloadProgressProvider: DownloadProgressProvider<Section>,
    private val unitDownloadProgressProvider: DownloadProgressProvider<Unit>,

    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : PresenterBase<CourseContentView>() {
    private var state: CourseContentView.State = CourseContentView.State.Idle
        set(value) {
            field = value
            view?.setState(value)
        }

    private val downloadsDisposable = CompositeDisposable()

    init {
        compositeDisposable += downloadsDisposable
        fetchCourseContent()
    }

    override fun attachView(view: CourseContentView) {
        super.attachView(view)
        view.setState(state)
        resolveDownloadProgressSubscription()
    }

    /**
     * Content
     */
    private fun fetchCourseContent(forceUpdate: Boolean = false) {
        if (state != CourseContentView.State.Idle
            && !(state == CourseContentView.State.NetworkError && forceUpdate)) return

        state = CourseContentView.State.Loading
        compositeDisposable += courseContentInteractor
            .getCourseContent()
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onNext  = { state = CourseContentView.State.CourseContentLoaded(it); resolveDownloadProgressSubscription() },
                onError = { state = CourseContentView.State.NetworkError }
            )
    }

    /**
     * Download progresses
     */
    private fun resolveDownloadProgressSubscription() {
        val items =
            (state as? CourseContentView.State.CourseContentLoaded)
            ?.courseContent
            ?.takeIf { view != null }
            ?: return

        downloadsDisposable.clear()
        val sectionIds = items
            .mapNotNull { item ->
                (item as? CourseContentItem.SectionItem)
                    ?.takeIf(CourseContentItem.SectionItem::isEnabled)
                    ?.section
                    ?.id
            }
            .toLongArray()

        subscribeForSectionsProgress(sectionIds)

        val unitIds = items
            .mapNotNull {
                (it as? CourseContentItem.UnitItem)?.takeIf(CourseContentItem.UnitItem::isEnabled)?.unit?.id
                    ?: (it as? CourseContentItem.UnitItemPlaceholder)?.unitId
            }
            .toLongArray()

        subscribeForUnitsProgress(unitIds)
    }

    private fun subscribeForSectionsProgress(sectionIds: LongArray) {
        downloadsDisposable += sectionDownloadProgressProvider
            .getProgress(*sectionIds)
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onError = { it.printStackTrace() },
                onNext  = { view?.updateSectionDownloadProgress(it) }
            )
    }

    private fun subscribeForUnitsProgress(unitIds: LongArray) {
        downloadsDisposable += unitDownloadProgressProvider
            .getProgress(*unitIds)
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onError = { it.printStackTrace() },
                onNext  = { view?.updateUnitDownloadProgress(it) }
            )
    }

    override fun detachView(view: CourseContentView) {
        downloadsDisposable.clear()
        super.detachView(view)
    }
}