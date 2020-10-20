package org.stepik.android.data.section.repository

import io.reactivex.Single
import org.stepik.android.data.base.repository.delegate.ListRepositoryDelegate
import org.stepik.android.data.section.source.SectionCacheDataSource
import org.stepik.android.data.section.source.SectionRemoteDataSource
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.section.repository.SectionRepository
import org.stepik.android.model.Section
import javax.inject.Inject

class SectionRepositoryImpl
@Inject
constructor(
    private val sectionCacheDataSource: SectionCacheDataSource,
    private val sectionRemoteDataSource: SectionRemoteDataSource
) : SectionRepository {
    private val delegate =
        ListRepositoryDelegate(
            sectionRemoteDataSource::getSections,
            sectionCacheDataSource::getSections,
            sectionCacheDataSource::saveSections
        )

    override fun getSections(sectionIds: List<Long>, primarySourceType: DataSourceType): Single<List<Section>> =
        delegate.get(sectionIds, primarySourceType, allowFallback = true)
}