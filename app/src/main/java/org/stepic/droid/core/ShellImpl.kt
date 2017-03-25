package org.stepic.droid.core

import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.web.Api
import javax.inject.Inject
import javax.inject.Singleton

@Deprecated("")
@Singleton
class ShellImpl
@Inject constructor(override val screenProvider: ScreenManager,
                    override val api: Api,
                    override val sharedPreferenceHelper: SharedPreferenceHelper) : Shell {

}
