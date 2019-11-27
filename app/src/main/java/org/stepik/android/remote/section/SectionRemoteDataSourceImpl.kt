package org.stepik.android.remote.section

import io.reactivex.Single
import io.reactivex.functions.Function
import org.stepik.android.data.section.source.SectionRemoteDataSource
import org.stepik.android.model.Section
import org.stepik.android.remote.base.chunkedSingleMap
import org.stepik.android.remote.section.model.SectionResponse
import org.stepik.android.remote.section.service.SectionService
import javax.inject.Inject

class SectionRemoteDataSourceImpl
@Inject
constructor(
    private val sectionService: SectionService
) : SectionRemoteDataSource {
    private val sectionResponseMapper = Function(SectionResponse::sections)

    override fun getSections(vararg sectionIds: Long): Single<List<Section>> =
        sectionIds
            .chunkedSingleMap { ids ->
                sectionService.getSections(ids)
                    .map(sectionResponseMapper)
            }
}