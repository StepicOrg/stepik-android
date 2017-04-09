package org.stepic.droid.core


import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.web.Api

@Deprecated("use without shell")
interface Shell {
    val api: Api
    val sharedPreferenceHelper: SharedPreferenceHelper
}
