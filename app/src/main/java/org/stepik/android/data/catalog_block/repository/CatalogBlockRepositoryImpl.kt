package org.stepik.android.data.catalog_block.repository

import io.reactivex.Single
import org.stepik.android.data.catalog_block.source.CatalogBlockRemoteDataSource
import org.stepik.android.domain.catalog_block.model.CatalogBlockItem
import org.stepik.android.domain.catalog_block.repository.CatalogBlockRepository
import javax.inject.Inject

class CatalogBlockRepositoryImpl
@Inject
constructor(
    private val catalogBlockRemoteDataSource: CatalogBlockRemoteDataSource
) : CatalogBlockRepository {
    override fun getCatalogBlocks(): Single<List<CatalogBlockItem>> =
        catalogBlockRemoteDataSource.getCatalogBlocks()
}