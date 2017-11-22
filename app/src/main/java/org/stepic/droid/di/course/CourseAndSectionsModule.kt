package org.stepic.droid.di.course

import dagger.Binds
import dagger.Module
import org.stepic.droid.core.downloadingstate.ProgressWatcher
import org.stepic.droid.core.downloadingstate.SectionProgressWatcher

@Module
interface CourseAndSectionsModule {

    @Binds
    fun bindProgressWatcher(sectionProgressWatcher: SectionProgressWatcher): ProgressWatcher

}
