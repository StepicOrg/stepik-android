package org.stepik.android.data.catalog_block.repository

import io.reactivex.Single
import org.stepik.android.data.catalog_block.source.CatalogBlockCacheDataSource
import org.stepik.android.data.catalog_block.source.CatalogBlockRemoteDataSource
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.catalog_block.model.CatalogBlockItem
import org.stepik.android.domain.catalog_block.repository.CatalogBlockRepository
import ru.nobird.android.domain.rx.doCompletableOnSuccess
import javax.inject.Inject

class CatalogBlockRepositoryImpl
@Inject
constructor(
    private val catalogBlockRemoteDataSource: CatalogBlockRemoteDataSource,
    private val catalogBlockCacheDataSource: CatalogBlockCacheDataSource
) : CatalogBlockRepository {
    override fun getCatalogBlocks(primarySourceType: DataSourceType): Single<List<CatalogBlockItem>> {
        val remoteSource = catalogBlockRemoteDataSource
            .getCatalogBlocks()
            .doCompletableOnSuccess { catalogBlockCacheDataSource.insertCatalogBlocks(it) }

        val cacheSource = catalogBlockCacheDataSource
            .getCatalogBlocks()

        return when (primarySourceType) {
            DataSourceType.REMOTE ->
                remoteSource
                    .onErrorResumeNext(cacheSource)

            DataSourceType.CACHE ->
                cacheSource
                    .filter(List<CatalogBlockItem>::isEmpty)
                    .switchIfEmpty(remoteSource)

            else ->
                throw IllegalArgumentException("Unsupported source type = $primarySourceType")
        }
    }
}