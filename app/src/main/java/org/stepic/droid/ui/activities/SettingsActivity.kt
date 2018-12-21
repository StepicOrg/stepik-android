package org.stepic.droid.ui.activities

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.MenuItem
import org.stepic.droid.R
import org.stepic.droid.base.SingleFragmentActivity
import org.stepic.droid.ui.fragments.SettingsFragment
import org.stepic.droid.ui.util.initCenteredToolbar

open class SettingsActivity : SingleFragmentActivity() {

    override fun createFragment(): Fragment = SettingsFragment.newInstance()

    override fun getLayoutResId(): Int = R.layout.activity_container_with_bar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpToolbar()
    }


    protected open fun setUpToolbar() {
        initCenteredToolbar(R.string.settings_title,
                showHomeButton = true,
                homeIndicator = closeIconDrawableRes)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                return if (sharedPreferenceHelper.authResponseFromStore == null) {
                    finish()
                    true
                } else {
                    super.onOptionsItemSelected(item)
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