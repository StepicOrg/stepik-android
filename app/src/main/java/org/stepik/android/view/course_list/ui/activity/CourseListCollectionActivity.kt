package org.stepik.android.view.course_list.ui.activity

import android.content.Context
import android.content.Intent
import android.view.MenuItem
import androidx.fragment.app.Fragment
import org.stepic.droid.base.SingleFragmentActivity
import org.stepik.android.model.CourseCollection
import org.stepik.android.view.course_list.ui.fragment.CourseListCollectionFragment

class CourseListCollectionActivity : SingleFragmentActivity() {
    companion object {
        private const val EXTRA_COURSE_COLLECTION = "course_collection"

        fun createIntent(context: Context, courseCollection: CourseCollection): Intent =
            Intent(context, CourseListCollectionActivity::class.java)
                .putExtra(EXTRA_COURSE_COLLECTION, courseCollection)
    }

    override fun createFragment(): Fragment =
        CourseListCollectionFragment.newInstance(
            courseCollection = intent.getParcelableExtra(EXTRA_COURSE_COLLECTION)
        )

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}