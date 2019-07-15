package org.stepik.android.remote.section

import io.reactivex.Single
import io.reactivex.functions.Function
import org.stepic.droid.web.Api
import org.stepik.android.remote.section.model.SectionResponse
import org.stepik.android.data.section.source.SectionRemoteDataSource
import org.stepik.android.model.Section
import org.stepik.android.remote.base.chunkedSingleMap
import javax.inject.Inject

class SectionRemoteDataSourceImpl
@Inject
constructor(
    private val api: Api
) : SectionRemoteDataSource {
    private val sectionResponseMapper = Function(SectionResponse::sections)

    override fun getSections(vararg sectionIds: Long): Single<List<Section>> =
        sectionIds
            .chunkedSingleMap { ids ->
                api.getSectionsRx(ids)
                    .map(sectionResponseMapper)
            }
}