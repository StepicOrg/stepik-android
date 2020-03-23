package org.stepik.android.view.course_list.ui.activity

import android.content.Context
import android.content.Intent
import android.view.MenuItem
import androidx.fragment.app.Fragment
import org.stepic.droid.base.SingleFragmentActivity
import org.stepik.android.view.course_list.ui.fragment.CourseListSearchFragment

class CourseListSearchActivity : SingleFragmentActivity() {
    companion object {
        private const val EXTRA_QUERY = "query"

        fun createIntent(context: Context, query: String): Intent =
            Intent(context, CourseListSearchActivity::class.java)
                .putExtra(EXTRA_QUERY, query)
    }

    override fun createFragment(): Fragment =
        CourseListSearchFragment.newInstance(
            query = intent.getStringExtra(EXTRA_QUERY)
        )

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}