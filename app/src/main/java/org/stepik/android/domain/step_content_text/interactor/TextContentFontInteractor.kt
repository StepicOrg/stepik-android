package org.stepik.android.domain.step_content_text.interactor

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepik.android.domain.step_content_text.model.FontSize
import javax.inject.Inject

class TextContentFontInteractor
@Inject
constructor(
    private val sharedPreferenceHelper: SharedPreferenceHelper
) {
    fun fetchTextContentFontSize(): Single<FontSize> =
        Single.fromCallable {
            val ordinal = sharedPreferenceHelper.stepContentFontSize
            FontSize.values()[ordinal]
        }

    fun setTextContentFontSize(fontSize: FontSize): Completable =
        Completable.fromAction {
            sharedPreferenceHelper.putStepContentFontSize(fontSize)
        }
}