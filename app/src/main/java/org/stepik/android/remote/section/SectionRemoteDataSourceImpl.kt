package org.stepik.android.remote.section

import io.reactivex.Single
import org.stepic.droid.web.Api
import org.stepic.droid.web.SectionsMetaResponse
import org.stepik.android.data.section.source.SectionRemoteDataSource
import org.stepik.android.model.Section
import javax.inject.Inject

class SectionRemoteDataSourceImpl
@Inject
constructor(
    private val api: Api
) : SectionRemoteDataSource {
    override fun getSections(vararg sectionIds: Long): Single<List<Section>> =
        if (sectionIds.isEmpty()) {
            Single.just(emptyList())
        } else {
            api.getSectionsRx(sectionIds)
                .map(SectionsMetaResponse::getSections)
        }
}