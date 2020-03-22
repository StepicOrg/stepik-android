package org.stepik.android.view.course_list.activity

import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import org.stepic.droid.base.SingleFragmentActivity
import org.stepik.android.domain.course_list.model.CourseListQuery
import org.stepik.android.view.course_list.ui.fragment.CourseCollectionFragment
import org.stepik.android.view.course_list.ui.fragment.CoursesQueryFragment

class CourseListActivity : SingleFragmentActivity() {
    companion object {
        private const val EXTRA_COURSE_LIST_QUERY = "course_list_query"
        private const val EXTRA_COURSE_IDS = "course_ids"

        fun createIntent(context: Context, courseListQuery: CourseListQuery): Intent =
            Intent(context, CourseListActivity::class.java)
                .putExtra(EXTRA_COURSE_LIST_QUERY, courseListQuery)

        fun createIntent(context: Context, courseIds: LongArray): Intent =
            Intent(context, CourseListActivity::class.java)
                .putExtra(EXTRA_COURSE_IDS, courseIds)
    }

    override fun createFragment(): Fragment {
        val courseListQuery = intent.getParcelableExtra<CourseListQuery>(EXTRA_COURSE_LIST_QUERY)
        val courseCollectionIds = intent.getLongArrayExtra(EXTRA_COURSE_IDS)

        return when {
            courseListQuery != null ->
                CoursesQueryFragment.newInstance(courseListQuery = courseListQuery)

            courseCollectionIds != null ->
                CourseCollectionFragment.newInstance(courseIds = courseCollectionIds)

            else ->
                Fragment()
        }
    }
}