package org.stepic.droid.di.section

import dagger.Binds
import dagger.Module
import org.stepic.droid.core.downloadingProgress.LessonProgressWatcher
import org.stepic.droid.core.downloadingProgress.ProgressWatcher

@Module
interface SectionModule {
    @Binds
    fun bindProgressWatcher(lessonProgressWatcher: LessonProgressWatcher): ProgressWatcher
}
