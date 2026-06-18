package org.stepik.android.view.injection.rubricator

import androidx.lifecycle.ViewModel
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import org.stepik.android.view.injection.base.ViewModelFactoryModule
import org.stepik.android.presentation.base.injection.ViewModelKey
import org.stepik.android.presentation.rubricator.RubricatorActionDispatcher
import org.stepik.android.presentation.rubricator.RubricatorFeature
import org.stepik.android.presentation.rubricator.RubricatorReducer
import org.stepik.android.presentation.rubricator.RubricatorViewModel
import ru.nobird.app.presentation.redux.container.wrapWithViewContainer
import ru.nobird.app.presentation.redux.dispatcher.wrapWithActionDispatcher
import ru.nobird.app.presentation.redux.feature.ReduxFeature

@Module(includes = [ViewModelFactoryModule::class])
object RubricatorPresentationModule {
    @Provides
    @IntoMap
    @ViewModelKey(RubricatorViewModel::class)
    internal fun provideRubricatorViewModel(
        rubricatorReducer: RubricatorReducer,
        rubricatorActionDispatcher: RubricatorActionDispatcher
    ): ViewModel =
        RubricatorViewModel(
            ReduxFeature(
                RubricatorFeature.State(
                    rubricatorState = RubricatorFeature.RubricatorState.Idle,
                    metaCategoryState = RubricatorFeature.MetaCategoryState.Empty,
                    courseListState = RubricatorFeature.CourseListState.Empty
                ), rubricatorReducer
            ).wrapWithActionDispatcher(rubricatorActionDispatcher)
                .wrapWithViewContainer()
        )
}