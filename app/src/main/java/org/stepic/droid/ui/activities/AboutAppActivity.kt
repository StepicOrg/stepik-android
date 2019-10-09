package org.stepic.droid.ui.activities

import android.os.Bundle
import androidx.core.app.Fragment
import android.view.MenuItem
import org.stepic.droid.R
import org.stepic.droid.base.SingleFragmentActivity
import org.stepic.droid.ui.fragments.AboutAppFragment
import org.stepic.droid.ui.util.initCenteredToolbar

open class AboutAppActivity : SingleFragmentActivity() {

    override fun createFragment(): Fragment {
        return AboutAppFragment.newInstance()
    }

    override fun getLayoutResId(): Int {
        return R.layout.activity_container_with_bar
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpToolbar()
    }


    private fun setUpToolbar() {
        initCenteredToolbar(R.string.about_app_title, true, closeIconDrawableRes)
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