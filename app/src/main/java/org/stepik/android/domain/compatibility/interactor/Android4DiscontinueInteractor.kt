package org.stepik.android.domain.compatibility.interactor

import android.os.Build
import org.stepic.droid.preferences.SharedPreferenceHelper
import javax.inject.Inject

class Android4DiscontinueInteractor
@Inject
constructor(
    private val sharedPreferenceHelper: SharedPreferenceHelper
) {
    fun isNeedShowDialog(): Boolean {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) return false

        val isWasShown = sharedPreferenceHelper.isAndroid4DiscontinueDialogWasShown
        if (!isWasShown) {
            sharedPreferenceHelper.setAndroid4DiscontinueDialogWasShown()
        }
        return !isWasShown
    }
}