package org.stepic.droid.util.resolvers

interface IMainMenuResolver {
    @Throws(IllegalArgumentException::class)
    fun getIndexOfFragment(clazz: Class<Any>): Int
}
