package org.stepic.droid.di.storage

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import org.stepic.droid.features.deadlines.storage.dao.DeadlinesBannerDao
import org.stepic.droid.features.deadlines.storage.operations.DeadlinesRecordOperations
import org.stepic.droid.features.stories.model.ViewedStoryTemplate
import org.stepic.droid.persistence.storage.dao.PersistentItemDao
import org.stepic.droid.persistence.storage.dao.PersistentStateDao
import org.stepic.droid.storage.dao.IDao
import org.stepic.droid.storage.operations.DatabaseFacade

@Component(modules = [StorageModule::class])
@StorageSingleton
interface StorageComponent {

    @Component.Builder
    interface Builder {
        fun build(): StorageComponent

        @BindsInstance
        fun context(context: Context): Builder
    }

    val databaseFacade: DatabaseFacade

    val deadlinesRecordOperations: DeadlinesRecordOperations
    val deadlinesBannerDao: DeadlinesBannerDao
    val persistentItemDao: PersistentItemDao
    val persistentStateDao: PersistentStateDao

    val viewedStoryTemplatesDao: IDao<ViewedStoryTemplate>
}
