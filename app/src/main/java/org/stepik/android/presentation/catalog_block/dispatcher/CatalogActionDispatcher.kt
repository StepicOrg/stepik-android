package org.stepik.android.presentation.catalog_block.dispatcher

import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.catalog.interactor.CatalogInteractor
import org.stepik.android.domain.catalog_block.model.CatalogBlockContent
import org.stepik.android.presentation.catalog_block.CatalogFeature
import org.stepik.android.presentation.course_list_redux.CourseListFeature
import org.stepik.android.presentation.course_list_redux.model.CatalogBlockStateWrapper
import ru.nobird.android.presentation.redux.dispatcher.RxActionDispatcher
import javax.inject.Inject

class CatalogActionDispatcher
@Inject
constructor(
    private val catalogInteractor: CatalogInteractor,
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : RxActionDispatcher<CatalogFeature.Action, CatalogFeature.Message>() {
    override fun handleAction(action: CatalogFeature.Action) {
        when (action) {
            is CatalogFeature.Action.FetchCatalogBlocks -> {
                compositeDisposable += catalogInteractor
                    .fetchCatalogBlocks()
                    .subscribeOn(backgroundScheduler)
                    .observeOn(mainScheduler)
                    .subscribeBy(
                        onSuccess = { catalogBlocks ->
                            val fullLists = catalogBlocks.filter { it.content is CatalogBlockContent.FullCourseList }
                            val catalogWrappers = fullLists.map { CatalogBlockStateWrapper.CourseList(catalogBlockItem = it, state = CourseListFeature.State.Loading) }
                            onNewMessage(CatalogFeature.Message.FetchCatalogBlocksSuccess(catalogWrappers))
                        },
                        onError = { onNewMessage(CatalogFeature.Message.FetchCatalogBlocksError) }
                    )
            }
        }
    }
}