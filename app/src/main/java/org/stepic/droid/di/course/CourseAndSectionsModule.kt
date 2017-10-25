package org.stepic.droid.di.course

import dagger.Binds
import dagger.Module
import org.stepic.droid.core.downloadingProgress.SectionProgressWatcher
import org.stepic.droid.core.downloadingProgress.ProgressWatcher

@Module
interface CourseAndSectionsModule {

    @Binds
    fun bindProgressWatcher(sectionProgressWatcher: SectionProgressWatcher): ProgressWatcher

}
