package org.stepik.android.view.step_quiz_fullscreen_code.ui.adapter

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.stepic.droid.R

class CodeStepQuizFullScreenPagerAdapter(
    context: Context
) : PagerAdapter() {

    private val layouts = listOf(
        LayoutInflater.from(context).inflate(R.layout.layout_step_quiz_code_fullscreen_instruction, null) to "Instruction",
        LayoutInflater.from(context).inflate(R.layout.layout_step_quiz_code_fullscreen_playground, null) to "Playground"
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
}