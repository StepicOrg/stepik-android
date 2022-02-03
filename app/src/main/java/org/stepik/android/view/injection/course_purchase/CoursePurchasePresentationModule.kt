package org.stepik.android.view.injection.course_purchase

import androidx.lifecycle.ViewModel
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import org.stepik.android.presentation.base.injection.ViewModelKey
import org.stepik.android.presentation.course_purchase.CoursePurchaseFeature
import org.stepik.android.presentation.course_purchase.CoursePurchaseViewModel
import org.stepik.android.presentation.course_purchase.dispatcher.CoursePurchaseActionDispatcher
import org.stepik.android.presentation.wishlist.dispatcher.WishlistOperationActionDispatcher
import org.stepik.android.presentation.course_purchase.reducer.CoursePurchaseReducer
import ru.nobird.app.core.model.safeCast
import ru.nobird.app.presentation.redux.container.wrapWithViewContainer
import ru.nobird.app.presentation.redux.dispatcher.transform
import ru.nobird.app.presentation.redux.dispatcher.wrapWithActionDispatcher
import ru.nobird.app.presentation.redux.feature.ReduxFeature

@Module
object CoursePurchasePresentationModule {
    @Provides
    @IntoMap
    @ViewModelKey(CoursePurchaseViewModel::class)
    internal fun provideCoursePurchasePresenter(
        coursePurchaseReducer: CoursePurchaseReducer,
        coursePurchaseActionDispatcher: CoursePurchaseActionDispatcher,
        wishlistOperationActionDispatcher: WishlistOperationActionDispatcher
    ): ViewModel =
        CoursePurchaseViewModel(
            ReduxFeature(CoursePurchaseFeature.State.Idle, coursePurchaseReducer)
                .wrapWithActionDispatcher(coursePurchaseActionDispatcher)
                .wrapWithActionDispatcher(
                    wishlistOperationActionDispatcher.transform(
                        transformAction = { it.safeCast<CoursePurchaseFeature.Action.WishlistAction>()?.action },
                        transformMessage = CoursePurchaseFeature.Message::WishlistMessage
                    )
                )
                .wrapWithViewContainer()
        )
}