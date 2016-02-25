package org.stepic.droid.util.resolvers

import org.stepic.droid.view.fragments.*

class MainMenuResolverImpl : IMainMenuResolver {
    @Throws(IllegalArgumentException::class)
    override fun getIndexOfFragment(clazz: Class<Any>): Int {
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
