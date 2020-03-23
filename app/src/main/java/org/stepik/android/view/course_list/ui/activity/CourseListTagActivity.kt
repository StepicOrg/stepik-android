package org.stepik.android.view.course_list.ui.activity

import android.content.Context
import android.content.Intent
import android.view.MenuItem
import androidx.fragment.app.Fragment
import org.stepic.droid.base.SingleFragmentActivity
import org.stepik.android.model.Tag
import org.stepik.android.view.course_list.ui.fragment.CourseListTagFragment

class CourseListTagActivity : SingleFragmentActivity() {
    companion object {
        private const val EXTRA_TAG = "tag"

        fun createIntent(context: Context, tag: Tag): Intent =
            Intent(context, CourseListTagActivity::class.java)
                .putExtra(EXTRA_TAG, tag)
    }

    override fun createFragment(): Fragment =
        CourseListTagFragment.newInstance(
            tag = intent.getParcelableExtra(EXTRA_TAG)
        )

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}