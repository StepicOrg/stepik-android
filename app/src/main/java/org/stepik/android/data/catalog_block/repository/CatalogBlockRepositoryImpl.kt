package org.stepik.android.data.catalog_block.repository

import io.reactivex.Maybe
import org.stepik.android.data.catalog_block.source.CatalogBlockCacheDataSource
import org.stepik.android.data.catalog_block.source.CatalogBlockRemoteDataSource
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.catalog_block.model.CatalogBlock
import org.stepik.android.domain.catalog_block.repository.CatalogBlockRepository
import ru.nobird.android.domain.rx.doCompletableOnSuccess
import javax.inject.Inject

class CatalogBlockRepositoryImpl
@Inject
constructor(
    private val catalogBlockRemoteDataSource: CatalogBlockRemoteDataSource,
    private val catalogBlockCacheDataSource: CatalogBlockCacheDataSource
) : CatalogBlockRepository {
    override fun getCatalogBlocks(language: String, primarySourceType: DataSourceType): Maybe<List<CatalogBlock>> {
        val remoteSource = catalogBlockRemoteDataSource
            .getCatalogBlocks(language)
            .doCompletableOnSuccess { catalogBlockCacheDataSource.insertCatalogBlocks(it) }

        val cacheSource = catalogBlockCacheDataSource
            .getCatalogBlocks(language)

        return when (primarySourceType) {
            DataSourceType.REMOTE ->
                remoteSource
                    .onErrorResumeNext(cacheSource)

            DataSourceType.CACHE ->
                cacheSource
                    .switchIfEmpty(remoteSource)

            else ->
                throw IllegalArgumentException("Unsupported source type = $primarySourceType")
        }
    }
}