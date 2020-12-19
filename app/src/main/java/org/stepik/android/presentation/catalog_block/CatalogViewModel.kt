package org.stepik.android.presentation.catalog_block

import ru.nobird.android.presentation.redux.container.ReduxViewContainer
import ru.nobird.android.view.redux.viewmodel.ReduxViewModel

class CatalogViewModel(
    reduxViewContainer: ReduxViewContainer<CatalogFeature.State, CatalogFeature.Message, CatalogFeature.Action.ViewAction>
) : ReduxViewModel<CatalogFeature.State, CatalogFeature.Message, CatalogFeature.Action.ViewAction>(reduxViewContainer)