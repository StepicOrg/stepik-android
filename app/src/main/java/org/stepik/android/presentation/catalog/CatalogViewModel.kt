package org.stepik.android.presentation.catalog

import ru.nobird.app.presentation.redux.container.ReduxViewContainer
import ru.nobird.android.view.redux.viewmodel.ReduxViewModel

class CatalogViewModel(
    reduxViewContainer: ReduxViewContainer<CatalogFeature.State, CatalogFeature.Message, CatalogFeature.Action.ViewAction>
) : ReduxViewModel<CatalogFeature.State, CatalogFeature.Message, CatalogFeature.Action.ViewAction>(reduxViewContainer)