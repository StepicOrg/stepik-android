package org.stepic.droid.core

import org.stepic.droid.di.AppSingleton
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.web.Api
import javax.inject.Inject

@Deprecated("")
@AppSingleton
class ShellImpl
@Inject constructor(override val api: Api,
                    override val sharedPreferenceHelper: SharedPreferenceHelper) : Shell {

}
