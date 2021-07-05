package org.stepik.android.view.course_benefits.ui.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.viewpager.widget.PagerAdapter
import org.stepic.droid.R

class CourseBenefitPagerAdapter(
    private val context: Context
) : PagerAdapter() {
    private val layouts: List<Pair<View, String>> = listOf(
        inflateLayout(R.layout.layout_purchases_and_refunds, R.string.course_benefits_purchases_and_refunds_tab),
        inflateLayout(R.layout.layout_purchases_and_refunds, R.string.course_benefits_monthly_tab)
    )

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = layouts[position].first
        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, view: Any) {
        container.removeView(layouts[position].first)
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean =
        view == `object`

    override fun getPageTitle(position: Int): CharSequence =
        layouts[position].second

    override fun getCount(): Int =
        layouts.size

    fun getViewAt(position: Int): View =
        layouts[position].first

    private fun inflateLayout(@LayoutRes layoutId: Int, @StringRes stringId: Int): Pair<View, String> =
        View.inflate(context, layoutId, null) to context.resources.getString(stringId)
}