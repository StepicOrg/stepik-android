package org.stepic.droid.adaptive.ui.activities

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatDelegate
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_adaptive_stats.*
import org.stepic.droid.R
import org.stepic.droid.adaptive.model.AdaptiveStatsTabs
import org.stepic.droid.adaptive.ui.adapters.AdaptiveStatsViewPagerAdapter
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

    private var courseId: Long = 0
    private var hasSavedInstanceState: Boolean = false
    private lateinit var adapter: AdaptiveStatsViewPagerAdapter
    private val onPageChangeListener = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(page: Int) {
            if (page == AdaptiveStatsTabs.RATING.ordinal) {
                analytic
                    .reportAmplitudeEvent(
                        AmplitudeAnalytic.Adaptive.RATING_OPENED,
                        mapOf(AmplitudeAnalytic.Adaptive.Params.COURSE to courseId.toString())
                    )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adaptive_stats)
        initCenteredToolbar(R.string.adaptive_stats_title, true)

        courseId = intent.getLongExtra(AppConstants.KEY_COURSE_LONG_ID, 0)
        hasSavedInstanceState = savedInstanceState != null

        adapter = AdaptiveStatsViewPagerAdapter(this, courseId)

        pager.adapter = adapter
        pager.offscreenPageLimit = adapter.itemCount

        TabLayoutMediator(tabLayout, pager) { tab, position ->
            tab.setText(AdaptiveStatsTabs.values()[position].fragmentTitleRes)
        }.attach()
    }

    override fun onResume() {
        super.onResume()
        pager.registerOnPageChangeCallback(onPageChangeListener)
        if (!hasSavedInstanceState && pager.currentItem == 0) {
            onPageChangeListener.onPageSelected(0)
        }
    }

    override fun onPause() {
        pager.unregisterOnPageChangeCallback(onPageChangeListener)
        super.onPause()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}