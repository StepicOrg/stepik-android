package org.stepik.android.data.section.source

import io.reactivex.Maybe
import io.reactivex.Single
import org.stepic.droid.util.maybeFirst
import org.stepik.android.model.Section
import org.stepik.android.remote.section.model.SectionResponse
import retrofit2.Call

interface SectionRemoteDataSource {
    fun getSectionRx(sectionId: Long): Maybe<Section> =
        getSectionsRx(sectionId).maybeFirst()

    fun getSectionsRx(vararg sectionIds: Long): Single<List<Section>>

    fun getSections(vararg sectionIds: Long): Call<SectionResponse>
}