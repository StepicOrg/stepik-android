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
        storiesActionDispatcher: StoriesActionDispatcher
    ): ViewModel =
        CatalogViewModel(
            ReduxFeature(CatalogFeature.State(storiesState = StoriesFeature.State.Idle), catalogReducer)
                .wrapWithActionDispatcher(catalogActionDispatcher)
                .wrapWithActionDispatcher(
                    storiesActionDispatcher.tranform(
                        transformAction = { it.safeCast<CatalogFeature.Action.StoriesAction>()?.action },
                        transformMessage = CatalogFeature.Message::StoriesMessage
                    )
                )
                .wrapWithViewContainer()
        )
}