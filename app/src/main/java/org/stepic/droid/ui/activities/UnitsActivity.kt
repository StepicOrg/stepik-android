package org.stepic.droid.ui.activities

import android.support.v4.app.Fragment
import org.stepic.droid.R
import org.stepic.droid.base.SingleFragmentActivity
import org.stepik.android.model.Section
import org.stepic.droid.ui.fragments.UnitsFragment
import org.stepic.droid.util.AppConstants

class UnitsActivity : SingleFragmentActivity() {

    override fun createFragment(): Fragment {
        val section = intent.getParcelableExtra<Section>(AppConstants.KEY_SECTION_BUNDLE)
        return UnitsFragment.newInstance(section)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_from_start, R.anim.slide_out_to_end)
    }
}
