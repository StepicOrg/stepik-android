package org.stepik.android.view.injection.catalog_block

import androidx.lifecycle.ViewModel
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import org.stepik.android.presentation.base.injection.ViewModelKey
import org.stepik.android.presentation.catalog_block.CatalogFeature
import org.stepik.android.presentation.catalog_block.CatalogViewModel
import org.stepik.android.presentation.catalog_block.dispatcher.CatalogActionDispatcher
import org.stepik.android.presentation.catalog_block.reducer.CatalogReducer
import org.stepik.android.presentation.course_list_redux.CourseListFeature
import org.stepik.android.presentation.course_list_redux.dispatcher.CourseListActionDispatcher
import org.stepik.android.presentation.filter.FiltersFeature
import org.stepik.android.presentation.filter.dispatcher.FiltersActionDispatcher
import org.stepik.android.presentation.stories.StoriesFeature
import org.stepik.android.presentation.stories.dispatcher.StoriesActionDispatcher
import ru.nobird.android.core.model.safeCast
import ru.nobird.android.presentation.redux.container.wrapWithViewContainer
import ru.nobird.android.presentation.redux.dispatcher.tranform
import ru.nobird.android.presentation.redux.dispatcher.wrapWithActionDispatcher
import ru.nobird.android.presentation.redux.feature.ReduxFeature

@Module
object CatalogBlockPresentationModule {
    @Provides
    @IntoMap
    @ViewModelKey(CatalogViewModel::class)
    internal fun provideCatalogBlockPresenter(
        catalogReducer: CatalogReducer,
        catalogActionDispatcher: CatalogActionDispatcher,
        storiesActionDispatcher: StoriesActionDispatcher,
        filtersActionDispatcher: FiltersActionDispatcher,
        courseListActionDispatcher: CourseListActionDispatcher
    ): ViewModel =
        CatalogViewModel(
            ReduxFeature(
                CatalogFeature.State(
                    storiesState = StoriesFeature.State.Idle,
                    filtersState = FiltersFeature.State.Idle,
                    collectionsState = CatalogFeature.CollectionsState.Idle // TODO Switch to idle
                ), catalogReducer
            )
                .wrapWithActionDispatcher(catalogActionDispatcher)
                .wrapWithActionDispatcher(
                    storiesActionDispatcher.tranform(
                        transformAction = { it.safeCast<CatalogFeature.Action.StoriesAction>()?.action },
                        transformMessage = CatalogFeature.Message::StoriesMessage
                    )
                )
                .wrapWithActionDispatcher(
                    filtersActionDispatcher.tranform(
                        transformAction = { it.safeCast<CatalogFeature.Action.FiltersAction>()?.action },
                        transformMessage = CatalogFeature.Message::FiltersMessage
                    )
                )
                .wrapWithActionDispatcher(
                    courseListActionDispatcher.tranform(
                        transformAction = { it.safeCast<CatalogFeature.Action.CourseListAction>()?.action },
                        transformMessage = { courseListMessage ->
                            val (id, message) = when (courseListMessage) {
                                is CourseListFeature.Message.InitMessage ->
                                    courseListMessage.id to courseListMessage
                                is CourseListFeature.Message.FetchCourseListSuccess ->
                                    courseListMessage.id to courseListMessage
                                is CourseListFeature.Message.FetchCourseListError ->
                                    courseListMessage.id to courseListMessage
                            }
                            CatalogFeature.Message.CourseListMessage(id, message)
                        }
                    )
                )
                .wrapWithViewContainer()
        )
}