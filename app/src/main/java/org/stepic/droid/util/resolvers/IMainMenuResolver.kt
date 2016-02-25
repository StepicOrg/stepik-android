package org.stepic.droid.util.resolvers

import android.support.v4.app.Fragment

interface IMainMenuResolver {
    @Throws(IllegalArgumentException::class)
    fun getIndexOfFragment(clazz: Class<out Fragment>): Int
}
