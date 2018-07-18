package org.stepic.droid.ui.activities

import android.app.Activity
import android.content.Intent
import android.support.v4.app.Fragment
import android.view.MenuItem
import org.stepic.droid.base.SingleFragmentActivity
import org.stepic.droid.ui.fragments.TagFragment
import org.stepik.android.model.structure.Tag

class TagActivity : SingleFragmentActivity() {

    companion object {
        private const val TAG_KEY = "tag_key"

        fun launch(parentActivity: Activity, tag: Tag) {
            val intent = Intent(parentActivity, TagActivity::class.java)
            intent.putExtra(TAG_KEY, tag)
            parentActivity.startActivity(intent)
        }
    }

    override fun createFragment(): Fragment {
        val tag = intent.getParcelableExtra<Tag>(TAG_KEY)
        return TagFragment.newInstance(tag)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
            when (item.itemId) {
                android.R.id.home -> {
                    finish()
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }

    override fun applyTransitionPrev() {
        //no-op
    }
}
