package org.stepik.android.presentation.catalog

import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.tags.interactor.TagsInteractor
import org.stepik.android.presentation.catalog.model.CatalogItem
import ru.nobird.android.presentation.base.PresenterBase
import javax.inject.Inject

class TagsPresenter
@Inject
constructor(
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler,
    private val tagsInteractor: TagsInteractor
) : PresenterBase<TagsView>(),
    CatalogItem {
    private var state: TagsView.State = TagsView.State.Idle
        set(value) {
            field = value
            view?.setState(value)
        }

    override fun attachView(view: TagsView) {
        super.attachView(view)
        view.setState(state)
    }

    init {
        fetchFeaturedTags()
    }

    fun fetchFeaturedTags(forceUpdate: Boolean = false) {
        if (state != TagsView.State.Idle && !forceUpdate) return
        compositeDisposable += tagsInteractor
            .fetchFeaturedTags()
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onSuccess = {
                    state = if (it.isNotEmpty()) {
                        TagsView.State.TagsLoaded(it)
                    } else {
                        TagsView.State.Empty
                    }
                },
                onError = {
                    state = TagsView.State.Empty
                }
            )
    }
}