package org.stepik.android.view.injection.lesson_demo

import androidx.lifecycle.ViewModel
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import org.stepik.android.model.Course
import org.stepik.android.presentation.base.injection.ViewModelKey
import org.stepik.android.presentation.lesson_demo.LessonDemoFeature
import org.stepik.android.presentation.lesson_demo.LessonDemoViewModel
import org.stepik.android.presentation.lesson_demo.dispatcher.LessonDemoActionDispatcher
import org.stepik.android.presentation.lesson_demo.reducer.LessonDemoReducer
import org.stepik.android.presentation.wishlist.WishlistOperationFeature
import org.stepik.android.presentation.wishlist.dispatcher.WishlistOperationActionDispatcher
import ru.nobird.android.core.model.safeCast
import ru.nobird.android.presentation.redux.container.wrapWithViewContainer
import ru.nobird.android.presentation.redux.dispatcher.transform
import ru.nobird.android.presentation.redux.dispatcher.wrapWithActionDispatcher
import ru.nobird.android.presentation.redux.feature.ReduxFeature

@Module
object LessonDemoPresentationModule {
    @Provides
    @IntoMap
    @ViewModelKey(LessonDemoViewModel::class)
    internal fun provideLessonDemoPresenter(
        course: Course,
        lessonDemoReducer: LessonDemoReducer,
        lessonDemoActionDispatcher: LessonDemoActionDispatcher,
        wishlistOperationActionDispatcher: WishlistOperationActionDispatcher
    ): ViewModel =
        LessonDemoViewModel(
            ReduxFeature(LessonDemoFeature.State(course, LessonDemoFeature.LessonDemoState.Idle, WishlistOperationFeature.State.Idle), lessonDemoReducer)
                .wrapWithActionDispatcher(lessonDemoActionDispatcher)
                .wrapWithActionDispatcher(
                    wishlistOperationActionDispatcher.transform(
                        transformAction = { it.safeCast<LessonDemoFeature.Action.WishlistAction>()?.action },
                        transformMessage = LessonDemoFeature.Message::WishlistMessage
                    )
                )
                .wrapWithViewContainer()
        )
}