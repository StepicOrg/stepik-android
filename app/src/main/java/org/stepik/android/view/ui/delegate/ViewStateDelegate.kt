package org.stepik.android.view.ui.delegate

import android.view.View

class ViewStateDelegate<S : Any> {
    private val viewMap: MutableMap<Class<out S>, Set<View>> = hashMapOf()
    private val views: MutableSet<View> = hashSetOf()

    fun addState(clazz: Class<out S>, vararg views: View) {
        val viewSet = views.toSet()
        this.viewMap[clazz] = viewSet
        this.views += viewSet
    }

    inline fun <reified C : S> addState(vararg views: View) {
        addState(C::class.java, *views)
    }

    fun switchState(newState: S) {
        val targetViews = viewMap[newState::class.java] ?: emptySet()
        val visibleViews = views.filter { it.visibility == View.VISIBLE }.toSet()

        (visibleViews - targetViews).forEach { it.visibility = View.GONE }
        (targetViews - visibleViews).forEach { it.visibility = View.VISIBLE }
    }
}