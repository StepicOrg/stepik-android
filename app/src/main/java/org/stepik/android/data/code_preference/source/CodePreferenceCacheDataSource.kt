package org.stepik.android.data.code_preference.source

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.cache.code_preference.model.CodePreference

interface CodePreferenceCacheDataSource {
    fun getCodePreference(languagesKey: String): Single<CodePreference>
    fun saveCodePreference(codePreference: CodePreference): Completable
}