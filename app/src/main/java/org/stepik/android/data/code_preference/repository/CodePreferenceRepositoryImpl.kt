package org.stepik.android.data.code_preference.repository

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.cache.code_preference.model.CodePreference
import org.stepik.android.data.code_preference.source.CodePreferenceCacheDataSource
import org.stepik.android.domain.code_preference.repository.CodePreferenceRepository
import javax.inject.Inject

class CodePreferenceRepositoryImpl
@Inject
constructor(
    private val codePreferenceCacheDataSource: CodePreferenceCacheDataSource
) : CodePreferenceRepository {
    override fun getCodePreference(languagesKey: List<String>): Single<CodePreference> =
        codePreferenceCacheDataSource
            .getCodePreference(languagesKey)
            .onErrorReturnItem(CodePreference.EMPTY)

    override fun saveCodePreference(codePreference: CodePreference): Completable =
        codePreferenceCacheDataSource.saveCodePreference(codePreference)
}