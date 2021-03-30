package org.stepik.android.view.injection.rubric

import dagger.Module
import dagger.Provides
import org.stepik.android.cache.base.database.AppDatabase
import org.stepik.android.cache.rubric.dao.RubricDao

@Module
class RubricDataModule {
    @Module
    companion object {
        @Provides
        @JvmStatic
        fun provideRubricDao(appDatabase: AppDatabase): RubricDao =
            appDatabase.rubricDao()
    }
}