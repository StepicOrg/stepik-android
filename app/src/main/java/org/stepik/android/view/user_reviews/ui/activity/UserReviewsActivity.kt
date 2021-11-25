package org.stepik.android.view.user_reviews.ui.activity

import android.content.Context
import android.content.Intent
import android.view.MenuItem
import androidx.fragment.app.Fragment
import org.stepic.droid.base.SingleFragmentActivity
import org.stepik.android.view.user_reviews.ui.fragment.UserReviewsFragment

class UserReviewsActivity : SingleFragmentActivity() {
    companion object {
        fun createIntent(context: Context): Intent =
            Intent(context, UserReviewsActivity::class.java)
    }

    override fun createFragment(): Fragment =
        UserReviewsFragment.newInstance()

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}