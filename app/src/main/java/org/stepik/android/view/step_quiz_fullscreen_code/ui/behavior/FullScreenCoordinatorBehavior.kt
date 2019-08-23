package org.stepik.android.view.step_quiz_fullscreen_code.ui.behavior

import android.content.Context
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup

class FullScreenCoordinatorBehavior(context: Context, attributeSet: AttributeSet) : CoordinatorLayout.Behavior<View>(context, attributeSet) {

    private var startHeight: Int? = null

    override fun layoutDependsOn(parent: CoordinatorLayout, child: View, dependency: View): Boolean =
        dependency is AppBarLayout

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
        if (dependency is AppBarLayout) {
            if (startHeight == null) {
                startHeight = child.height
            }
            child.y = dependency.y + dependency.height
            child.layoutParams = (child.layoutParams as ViewGroup.MarginLayoutParams)
                .apply {
                    bottomMargin = (dependency.height + dependency.y).toInt()
                }
        }
        return false
    }
}