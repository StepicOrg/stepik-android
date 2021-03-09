package org.stepik.android.domain.code_preference.repository

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.cache.code_preference.model.CodePreference

interface CodePreferenceRepository {
    fun getCodePreference(languagesKey: Set<String>): Single<CodePreference>
    fun saveCodePreference(codePreference: CodePreference): Completable
}