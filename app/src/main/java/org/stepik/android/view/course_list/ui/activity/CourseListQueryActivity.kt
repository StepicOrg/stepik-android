package org.stepik.android.view.course_list.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import android.view.MenuItem
import androidx.fragment.app.Fragment
import org.stepic.droid.base.SingleFragmentActivity
import org.stepik.android.domain.course_list.model.CourseListQuery
import org.stepik.android.view.course_list.ui.fragment.CourseListQueryFragment

class CourseListQueryActivity : SingleFragmentActivity() {
    companion object {
        private const val EXTRA_COURSE_LIST_TITLE = "course_list_title"
        private const val EXTRA_COURSE_LIST_QUERY = "course_list_query"

        fun createIntent(context: Context, courseListTitle: String, courseListQuery: CourseListQuery): Intent =
            Intent(context, CourseListQueryActivity::class.java)
                .putExtra(EXTRA_COURSE_LIST_TITLE, courseListTitle)
                .putExtra(EXTRA_COURSE_LIST_QUERY, courseListQuery as Parcelable)
    }

    override fun createFragment(): Fragment =
        CourseListQueryFragment.newInstance(
            courseListTitle = intent.getStringExtra(EXTRA_COURSE_LIST_TITLE),
            courseListQuery = intent.getParcelableExtra(EXTRA_COURSE_LIST_QUERY)
        )

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}