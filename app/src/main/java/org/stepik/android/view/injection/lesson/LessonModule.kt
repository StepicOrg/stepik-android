package org.stepik.android.view.injection.lesson

import android.arch.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import org.stepik.android.presentation.base.injection.ViewModelKey
import org.stepik.android.presentation.lesson.LessonPresenter

@Module
abstract class LessonModule {
    /**
     * Presentation
     */
    @Binds
    @IntoMap
    @ViewModelKey(LessonPresenter::class)
    internal abstract fun bindLessonPresenter(lessonPresenter: LessonPresenter): ViewModel
}