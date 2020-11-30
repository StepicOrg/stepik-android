package org.stepik.android.presentation.stories

import org.stepik.android.presentation.catalog.model.CatalogItem
import ru.nobird.android.presentation.redux.container.ReduxViewContainer
import ru.nobird.android.view.redux.viewmodel.ReduxViewModel

class StoriesViewModel(
    reduxViewContainer: ReduxViewContainer<StoriesFeature.State, StoriesFeature.Message, StoriesFeature.Action.ViewAction>
) : ReduxViewModel<StoriesFeature.State, StoriesFeature.Message, StoriesFeature.Action.ViewAction>(reduxViewContainer), CatalogItem