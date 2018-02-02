package org.stepic.droid.adaptive.ui.activities

import android.os.Bundle
import android.view.MenuItem
import org.stepic.droid.R
import org.stepic.droid.base.FragmentActivityBase
import org.stepic.droid.ui.util.initCenteredToolbar

class AdaptiveStatsActivity : FragmentActivityBase() {
    companion object {
        const val PAGE_KEY = "page"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adaptive_stats)
        initCenteredToolbar(R.string.adaptive_stats_title, true)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}