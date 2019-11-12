package org.stepik.android.view.injection.tags

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepik.android.data.tags.source.TagsRemoteDataSource
import org.stepik.android.remote.tags.TagsRemoteDataSourceImpl
import org.stepik.android.remote.tags.service.TagsService
import org.stepik.android.view.injection.base.Authorized
import retrofit2.Retrofit

@Module
abstract class TagsDataModule {
    @Binds
    internal abstract fun bindTagsRemoteDataSource(
        tagsRemoteDataSourceImpl: TagsRemoteDataSourceImpl
    ): TagsRemoteDataSource

    @Module
    companion object {
        @Provides
        @JvmStatic
        internal fun provideTagsService(@Authorized retrofit: Retrofit): TagsService =
            retrofit.create(TagsService::class.java)
    }
}