package org.stepic.droid.adaptive.ui.activities

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatDelegate
import androidx.viewpager.widget.ViewPager
import kotlinx.android.synthetic.main.activity_adaptive_stats.*
import org.stepic.droid.R
import org.stepic.droid.adaptive.ui.adapters.AdaptiveStatsViewPagerAdapter
import org.stepic.droid.adaptive.ui.fragments.AdaptiveRatingFragment
import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.base.FragmentActivityBase
import org.stepic.droid.ui.util.initCenteredToolbar
import org.stepic.droid.util.AppConstants

class AdaptiveStatsActivity : FragmentActivityBase() {
    companion object {
        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adaptive_stats)
        initCenteredToolbar(R.string.adaptive_stats_title, true)

        val courseId = intent.getLongExtra(AppConstants.KEY_COURSE_LONG_ID, 0)

        val adapter = AdaptiveStatsViewPagerAdapter(supportFragmentManager, this, courseId)
        pager.adapter = adapter
        pager.offscreenPageLimit = adapter.count

        val onPageChangeListener = object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(page: Int) {
                if (adapter.getItem(page) is AdaptiveRatingFragment) {
                    analytic
                        .reportAmplitudeEvent(
                            AmplitudeAnalytic.Adaptive.RATING_OPENED,
                            mapOf(AmplitudeAnalytic.Adaptive.Params.COURSE to courseId.toString())
                        )
                }
            }
        }
        pager.addOnPageChangeListener(onPageChangeListener)

        if (savedInstanceState == null && pager.currentItem == 0) {
            onPageChangeListener.onPageSelected(0)
        }

        tabLayout.setupWithViewPager(pager)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}