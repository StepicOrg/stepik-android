package org.stepik.android.view.injection.code_preference

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepik.android.cache.base.database.AppDatabase
import org.stepik.android.cache.code_preference.CodePreferenceCacheDataSourceImpl
import org.stepik.android.cache.code_preference.dao.CodePreferenceDao
import org.stepik.android.data.code_preference.repository.CodePreferenceRepositoryImpl
import org.stepik.android.data.code_preference.source.CodePreferenceCacheDataSource
import org.stepik.android.domain.code_preference.repository.CodePreferenceRepository

@Module
abstract class CodePreferenceDataModule {
    @Binds
    internal abstract fun bindCodePreferenceRepository(
        codePreferenceRepositoryImpl: CodePreferenceRepositoryImpl
    ): CodePreferenceRepository

    @Binds
    internal abstract fun bindCodePreferenceCacheDataSource(
        codePreferenceCacheDataSourceImpl: CodePreferenceCacheDataSourceImpl
    ): CodePreferenceCacheDataSource

    @Module
    companion object {
        @Provides
        @JvmStatic
        fun provideCodePreferenceDao(appDatabase: AppDatabase): CodePreferenceDao =
            appDatabase.codePreferenceDao()
    }
}