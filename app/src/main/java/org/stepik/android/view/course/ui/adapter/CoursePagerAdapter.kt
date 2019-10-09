package org.stepik.android.view.course.ui.adapter

import android.content.Context
import androidx.core.app.Fragment
import androidx.core.app.FragmentManager
import androidx.core.app.FragmentPagerAdapter
import android.view.ViewGroup
import org.stepic.droid.R
import org.stepik.android.view.course_content.ui.fragment.CourseContentFragment
import org.stepik.android.view.course_info.ui.fragment.CourseInfoFragment
import org.stepik.android.view.course_reviews.ui.fragment.CourseReviewsFragment
import org.stepik.android.view.fragment_pager.ActiveFragmentPagerAdapter

class CoursePagerAdapter(
    courseId: Long,
    context: Context,
    fragmentManager: FragmentManager
) : FragmentPagerAdapter(fragmentManager), ActiveFragmentPagerAdapter {
    private val fragments = listOf(
        { CourseInfoFragment.newInstance(courseId) }    to context.getString(R.string.course_tab_info),
        { CourseReviewsFragment.newInstance(courseId) } to context.getString(R.string.course_tab_reviews),
        { CourseContentFragment.newInstance(courseId) } to context.getString(R.string.course_tab_modules)
    )

    private val _activeFragments = mutableMapOf<Int, Fragment>()
    override val activeFragments: Map<Int, Fragment>
        get() = _activeFragments

    override fun getItem(position: Int): Fragment =
        fragments[position].first.invoke()

    override fun getCount(): Int =
        fragments.size

    override fun getPageTitle(position: Int): CharSequence =
        fragments[position].second

    override fun instantiateItem(container: ViewGroup, position: Int): Any =
        super
            .instantiateItem(container, position)
            .also {
                (it as? Fragment)?.let { fragment ->  _activeFragments[position] = fragment }
            }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        _activeFragments.remove(position)
        super.destroyItem(container, position, `object`)
    }
}