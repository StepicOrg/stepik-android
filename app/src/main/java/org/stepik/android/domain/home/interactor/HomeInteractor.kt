package org.stepik.android.domain.home.interactor

import org.stepic.droid.preferences.SharedPreferenceHelper
import javax.inject.Inject

class HomeInteractor
@Inject
constructor(
    private val sharedPreferenceHelper: SharedPreferenceHelper
) {
    fun isUserAuthorized(): Boolean =
        sharedPreferenceHelper.authResponseFromStore != null
}