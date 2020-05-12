package org.stepik.android.view.course_list.ui.adapter.viewpager

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import org.stepic.droid.R
import org.stepik.android.domain.course_list.model.CourseListUserQuery
import org.stepik.android.presentation.course_list.model.CourseListUserType
import org.stepik.android.view.course_list.ui.fragment.CourseListUserFragment

class CourseListUserPagerAdapter(
    context: Context,
    fragmentManager: FragmentManager
) : FragmentPagerAdapter(fragmentManager,  BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    private val fragments = listOf(
        { CourseListUserFragment.newInstance(CourseListUserType.ALL, CourseListUserQuery(page = 1)) } to context.getString(R.string.course_list_user_all_courses_tab),
        { CourseListUserFragment.newInstance(CourseListUserType.FAVORITE, CourseListUserQuery(page = 1, isFavorite = true)) } to context.getString(R.string.course_list_user_favorites_tab),
        { CourseListUserFragment.newInstance(CourseListUserType.ARCHIVED, CourseListUserQuery(page = 1, isArchived = true)) } to context.getString(R.string.course_list_user_archive_tab)
    )

    override fun getItem(position: Int): Fragment =
        fragments[position].first.invoke()

    override fun getCount(): Int =
        fragments.size

    override fun getPageTitle(position: Int): CharSequence =
        fragments[position].second
}