package org.stepik.android.view.injection.course_purchase

import androidx.lifecycle.ViewModel
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import org.stepik.android.presentation.base.injection.ViewModelKey
import org.stepik.android.presentation.course_purchase.CoursePurchaseFeature
import org.stepik.android.presentation.course_purchase.CoursePurchaseViewModel
import org.stepik.android.presentation.course_purchase.dispatcher.CoursePurchaseActionDispatcher
import org.stepik.android.presentation.course_purchase.reducer.CoursePurchaseReducer
import ru.nobird.android.presentation.redux.container.wrapWithViewContainer
import ru.nobird.android.presentation.redux.dispatcher.wrapWithActionDispatcher
import ru.nobird.android.presentation.redux.feature.ReduxFeature

@Module
object CoursePurchasePresentationModule {
    @Provides
    @IntoMap
    @ViewModelKey(CoursePurchaseViewModel::class)
    internal fun provideCoursePurchasePresenter(
        coursePurchaseReducer: CoursePurchaseReducer,
        coursePurchaseActionDispatcher: CoursePurchaseActionDispatcher
    ): ViewModel =
        CoursePurchaseViewModel(
            ReduxFeature(CoursePurchaseFeature.State.Idle, coursePurchaseReducer)
                .wrapWithActionDispatcher(coursePurchaseActionDispatcher)
                .wrapWithViewContainer()
        )
}