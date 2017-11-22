package org.stepic.droid.di.section

import dagger.Binds
import dagger.Module
import org.stepic.droid.core.downloadingprogress.LessonProgressWatcher
import org.stepic.droid.core.downloadingprogress.ProgressWatcher

@Module
interface SectionModule {
    @Binds
    fun bindProgressWatcher(lessonProgressWatcher: LessonProgressWatcher): ProgressWatcher
}
