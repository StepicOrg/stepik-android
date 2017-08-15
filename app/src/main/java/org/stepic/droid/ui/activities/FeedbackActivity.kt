package org.stepic.droid.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import org.stepic.droid.R
import org.stepic.droid.base.SingleFragmentActivity
import org.stepic.droid.ui.fragments.FeedbackFragment

class FeedbackActivity : SingleFragmentActivity() {
    override fun createFragment() = FeedbackFragment.newInstance()


    var toolbar: Toolbar? = null;

    override fun getLayoutResId(): Int {
        return R.layout.activity_container_with_bar
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawable(null)
        setTitle(R.string.feedback_title)
        toolbar = findViewById<Toolbar>(R.id.toolbar)
        setUpToolbar()
    }


    private fun setUpToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeAsUpIndicator(closeIconDrawableRes)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                finish();
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(org.stepic.droid.R.anim.no_transition, org.stepic.droid.R.anim.push_down)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == TextFeedbackActivity.requestCode && resultCode == Activity.RESULT_OK) {
            finish()
        }
    }
}
