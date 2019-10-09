package org.stepic.droid.ui.activities

import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import org.stepic.droid.R
import org.stepic.droid.base.SingleFragmentActivity
import org.stepic.droid.ui.fragments.AboutAppFragment
import org.stepic.droid.ui.util.initCenteredToolbar

open class AboutAppActivity : SingleFragmentActivity() {

    override fun createFragment(): Fragment =
        AboutAppFragment.newInstance()

    override fun getLayoutResId(): Int =
        R.layout.activity_container_with_bar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initCenteredToolbar(R.string.about_app_title, true, closeIconDrawableRes)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean =
        when (item?.itemId) {
            android.R.id.home -> {
                 if (sharedPreferenceHelper.authResponseFromStore == null) {
                    finish()
                    true
                } else {
                    super.onOptionsItemSelected(item)
                }
            }
            else ->
                super.onOptionsItemSelected(item)
        }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.no_transition, R.anim.push_down)
    }
}