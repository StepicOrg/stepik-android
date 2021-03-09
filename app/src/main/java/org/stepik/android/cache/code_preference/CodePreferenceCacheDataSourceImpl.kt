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
    companion object {
        private const val PREFIX = "["
        private const val POSTFIX = "]"
        private const val SEPARATOR = "__,__"
    }
    override fun getCodePreference(languagesKey: Set<String>): Single<CodePreference> =
        codePreferenceDao.getCodePreferences(mapLanguagesKeyToString(languagesKey))

    override fun saveCodePreference(codePreference: CodePreference): Completable =
        codePreferenceDao.saveCodePreference(codePreference)

    private fun mapLanguagesKeyToString(languagesKey: Set<String>): String =
        languagesKey.joinToString(separator = SEPARATOR, transform = { "$PREFIX$it$POSTFIX" })
}