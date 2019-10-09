package org.stepik.android.view.step_quiz_fullscreen_code.ui.adapter

import android.content.Context
import android.support.annotation.LayoutRes
import android.support.annotation.StringRes
import androidx.core.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import org.stepic.droid.R

class CodeStepQuizFullScreenPagerAdapter(
    private val context: Context
) : PagerAdapter() {

    private val layouts = listOf(
        inflateLayout(R.layout.layout_step_quiz_code_fullscreen_instruction,  R.string.step_quiz_code_full_screen_instruction_tab),
        inflateLayout(R.layout.layout_step_quiz_code_fullscreen_playground, R.string.step_quiz_code_full_screen_code_tab)
    )

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = layouts[position].first
        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, view: Any) {
        container.removeView(layouts[position].first)
    }

    override fun isViewFromObject(p0: View, p1: Any): Boolean =
        p0 == p1

    override fun getPageTitle(position: Int): CharSequence =
        layouts[position].second

    override fun getCount(): Int =
        layouts.size

    fun getViewAt(position: Int): View =
        layouts[position].first

    private fun inflateLayout(@LayoutRes layoutId: Int, @StringRes stringId: Int): Pair<View, String> =
        View.inflate(context, layoutId, null) to context.resources.getString(stringId)
}