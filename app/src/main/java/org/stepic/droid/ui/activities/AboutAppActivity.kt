package org.stepic.droid.ui.activities

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import org.stepic.droid.R
import org.stepic.droid.base.SingleFragmentActivity
import org.stepic.droid.ui.fragments.AboutAppFragment

open class AboutAppActivity : SingleFragmentActivity() {

    override fun createFragment(): Fragment? {
        return AboutAppFragment.newInstance()
    }

    var toolbar: Toolbar? = null;

    override fun getLayoutResId(): Int {
        return R.layout.activity_container_with_bar
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(R.string.about_app_title)
        toolbar = findViewById(R.id.toolbar) as Toolbar
        setUpToolbar()
    }


    private fun setUpToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_close_white_24dp)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                if (sharedPreferenceHelper.authResponseFromStore == null) {
                    finish();
                    return true
                } else {
                    return super.onOptionsItemSelected(item)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(org.stepic.droid.R.anim.no_transition, org.stepic.droid.R.anim.push_down)
    }
}