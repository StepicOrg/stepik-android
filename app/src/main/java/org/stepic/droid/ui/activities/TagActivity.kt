package org.stepic.droid.ui.activities

import android.app.Activity
import android.content.Intent
import android.support.v4.app.Fragment
import org.stepic.droid.base.SingleFragmentActivity
import org.stepic.droid.model.Tag
import org.stepic.droid.ui.fragments.TagFragment

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
}
