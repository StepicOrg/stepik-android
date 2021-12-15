package org.stepik.android.view.injection.lesson_demo

import androidx.lifecycle.ViewModel
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import org.stepik.android.presentation.base.injection.ViewModelKey
import org.stepik.android.presentation.lesson_demo.LessonDemoFeature
import org.stepik.android.presentation.lesson_demo.LessonDemoViewModel
import org.stepik.android.presentation.lesson_demo.dispatcher.LessonDemoActionDispatcher
import org.stepik.android.presentation.lesson_demo.reducer.LessonDemoReducer
import ru.nobird.android.presentation.redux.container.wrapWithViewContainer
import ru.nobird.android.presentation.redux.dispatcher.wrapWithActionDispatcher
import ru.nobird.android.presentation.redux.feature.ReduxFeature

@Module
object LessonDemoPresentationModule {
    @Provides
    @IntoMap
    @ViewModelKey(LessonDemoViewModel::class)
    internal fun provideLessonDemoPresenter(
        lessonDemoReducer: LessonDemoReducer,
        lessonDemoActionDispatcher: LessonDemoActionDispatcher
    ): ViewModel =
        LessonDemoViewModel(
            ReduxFeature(LessonDemoFeature.State.Idle, lessonDemoReducer)
                .wrapWithActionDispatcher(lessonDemoActionDispatcher)
                .wrapWithViewContainer()
        )
}