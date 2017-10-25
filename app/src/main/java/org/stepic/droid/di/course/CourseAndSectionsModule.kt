package org.stepic.droid.di.course

import dagger.Binds
import dagger.Module
import org.stepic.droid.core.downloadingProgress.ProgressSectionWatcher
import org.stepic.droid.core.downloadingProgress.ProgressWatcher

@Module
interface CourseAndSectionsModule {

    @Binds
    fun bindProgressWatcher(progressSectionWatcher: ProgressSectionWatcher): ProgressWatcher

}
