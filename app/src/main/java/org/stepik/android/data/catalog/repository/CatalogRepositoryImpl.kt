package org.stepik.android.data.catalog.repository

import io.reactivex.Maybe
import org.stepik.android.data.catalog.source.CatalogCacheDataSource
import org.stepik.android.data.catalog.source.CatalogRemoteDataSource
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.catalog.model.CatalogBlock
import org.stepik.android.domain.catalog.repository.CatalogRepository
import ru.nobird.android.domain.rx.doCompletableOnSuccess
import javax.inject.Inject

class CatalogRepositoryImpl
@Inject
constructor(
    private val catalogRemoteDataSource: CatalogRemoteDataSource,
    private val catalogCacheDataSource: CatalogCacheDataSource
) : CatalogRepository {
    override fun getCatalogBlocks(language: String, primarySourceType: DataSourceType): Maybe<List<CatalogBlock>> {
        val remoteSource = catalogRemoteDataSource
            .getCatalogBlocks(language)
            .doCompletableOnSuccess { catalogCacheDataSource.insertCatalogBlocks(it) }

        val cacheSource = catalogCacheDataSource
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