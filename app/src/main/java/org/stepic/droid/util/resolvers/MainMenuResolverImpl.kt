package org.stepic.droid.util.resolvers

import android.support.v4.app.Fragment
import org.stepic.droid.view.fragments.*

class MainMenuResolverImpl : IMainMenuResolver {
    @Throws(IllegalArgumentException::class)
    override fun getIndexOfFragment(clazz: Class<out Fragment>): Int {
        when (clazz) {
            MyCoursesFragment::class.java -> return 0
            FindCoursesFragment::class.java -> return 1
            DownloadsFragment::class.java -> return 2
            SettingsFragment::class.java -> return 3
            FeedbackFragment::class.java -> return 4
        }
        throw IllegalArgumentException("trial of resolve fragment not in menu")
    }
}
