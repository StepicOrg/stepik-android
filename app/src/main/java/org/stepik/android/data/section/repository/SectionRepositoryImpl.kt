package org.stepik.android.data.section.repository

import io.reactivex.Single
import org.stepic.droid.util.doCompletableOnSuccess
import org.stepic.droid.util.requireSize
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
    override fun getSections(vararg sectionIds: Long, primarySourceType: DataSourceType): Single<List<Section>> {
        val remoteSource = sectionRemoteDataSource
            .getSections(*sectionIds)
            .doCompletableOnSuccess(sectionCacheDataSource::saveSections)

        val cacheSource = sectionCacheDataSource
            .getSections(*sectionIds)

        return when (primarySourceType) {
            DataSourceType.REMOTE ->
                remoteSource.onErrorResumeNext(cacheSource.requireSize(sectionIds.size))

            DataSourceType.CACHE ->
                cacheSource.flatMap { cachedSections ->
                    val ids = (sectionIds.toList() - cachedSections.map(Section::id)).toLongArray()
                    sectionRemoteDataSource
                        .getSections(*ids)
                        .doCompletableOnSuccess(sectionCacheDataSource::saveSections)
                        .map { remoteSections -> cachedSections + remoteSections }
                }

            else ->
                throw IllegalArgumentException("Unsupported source type = $primarySourceType")
        }.map { sections -> sections.sortedBy { sectionIds.indexOf(it.id) } }
    }
}