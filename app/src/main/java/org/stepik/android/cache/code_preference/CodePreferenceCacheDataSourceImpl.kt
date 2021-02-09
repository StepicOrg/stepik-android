package org.stepik.android.cache.code_preference

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.cache.code_preference.dao.CodePreferenceDao
import org.stepik.android.cache.code_preference.model.CodePreference
import org.stepik.android.data.code_preference.source.CodePreferenceCacheDataSource
import javax.inject.Inject

class CodePreferenceCacheDataSourceImpl
@Inject
constructor(
    private val codePreferenceDao: CodePreferenceDao
) : CodePreferenceCacheDataSource {
    override fun getCodePreference(languagesKey: String): Single<CodePreference> =
        codePreferenceDao.getCodePreferences(languagesKey)

    override fun saveCodePreference(codePreference: CodePreference): Completable =
        codePreferenceDao.saveCodePreference(codePreference)
}