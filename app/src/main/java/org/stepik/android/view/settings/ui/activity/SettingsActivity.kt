package org.stepik.android.view.settings.ui.activity

import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import org.stepic.droid.R
import org.stepic.droid.ui.activities.SmartLockActivityBase
import org.stepic.droid.ui.util.initCenteredToolbar
import org.stepik.android.view.settings.ui.fragment.SettingsFragment

open class SettingsActivity : SmartLockActivityBase(), SettingsFragment.Companion.SignOutListener {

    open fun createFragment(): Fragment =
        SettingsFragment.newInstance()

    private fun getLayoutResId(): Int =
        R.layout.activity_container_with_bar

    protected var fragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutResId())
        val fm = supportFragmentManager
        fragment = fm.findFragmentById(R.id.fragmentContainer)

        if (fragment == null) {
            val fragment = createFragment().also { this.fragment = it }
            fm.beginTransaction()
                .add(R.id.fragmentContainer, fragment)
                .commit()
        }
        setUpToolbar()

        initGoogleApiClient(withAuth = true)
    }

    protected open fun setUpToolbar() {
        initCenteredToolbar(R.string.settings_title,
                showHomeButton = true,
                homeIndicator = closeIconDrawableRes)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean =
        when (item?.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else ->
                super.onOptionsItemSelected(item)
        }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.no_transition, R.anim.push_down)
    }

    override fun onSignOut() {
        signOutFromGoogle()
    }
}