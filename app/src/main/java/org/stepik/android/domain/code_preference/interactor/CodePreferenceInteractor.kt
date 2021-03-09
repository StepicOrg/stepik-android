package org.stepik.android.domain.code_preference.interactor

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.cache.code_preference.model.CodePreference
import org.stepik.android.domain.code_preference.repository.CodePreferenceRepository
import javax.inject.Inject

class CodePreferenceInteractor
@Inject
constructor(
    private val codePreferenceRepository: CodePreferenceRepository
) {
    fun getCodePreference(languagesKey: List<String>): Single<CodePreference> =
        codePreferenceRepository.getCodePreference(languagesKey)

    fun saveCodePreference(codePreference: CodePreference): Completable =
        codePreferenceRepository.saveCodePreference(codePreference)
}