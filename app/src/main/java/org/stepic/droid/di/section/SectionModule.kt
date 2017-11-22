package org.stepic.droid.di.section

import dagger.Binds
import dagger.Module
import org.stepic.droid.core.downloadingstate.LessonProgressWatcher
import org.stepic.droid.core.downloadingstate.ProgressWatcher

@Module
interface SectionModule {
    @Binds
    fun bindProgressWatcher(lessonProgressWatcher: LessonProgressWatcher): ProgressWatcher
}
