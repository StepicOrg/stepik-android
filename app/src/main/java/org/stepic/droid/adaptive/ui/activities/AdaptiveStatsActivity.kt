package org.stepic.droid.adaptive.ui.activities

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatDelegate
import androidx.viewpager2.widget.ViewPager2
import dev.androidbroadcast.vbpd.viewBinding
import com.google.android.material.tabs.TabLayoutMediator
import org.stepic.droid.R
import org.stepic.droid.adaptive.model.AdaptiveStatsTabs
import org.stepic.droid.adaptive.ui.adapters.AdaptiveStatsViewPagerAdapter
import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.base.FragmentActivityBase
import org.stepic.droid.databinding.ActivityAdaptiveStatsBinding
import org.stepic.droid.ui.util.initCenteredToolbar
import org.stepic.droid.util.AppConstants

class AdaptiveStatsActivity : FragmentActivityBase() {
    companion object {
        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }
    }

    private val adaptiveStatsBinding: ActivityAdaptiveStatsBinding by viewBinding(ActivityAdaptiveStatsBinding::bind)
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

        adaptiveStatsBinding.pager.adapter = adapter
        adaptiveStatsBinding.pager.offscreenPageLimit = adapter.itemCount

        TabLayoutMediator(adaptiveStatsBinding.tabLayout, adaptiveStatsBinding.pager) { tab, position ->
            tab.setText(AdaptiveStatsTabs.values()[position].fragmentTitleRes)
        }.attach()
    }

    override fun onResume() {
        super.onResume()
        adaptiveStatsBinding.pager.registerOnPageChangeCallback(onPageChangeListener)
        if (!hasSavedInstanceState && adaptiveStatsBinding.pager.currentItem == 0) {
            onPageChangeListener.onPageSelected(0)
        }
    }

    override fun onPause() {
        adaptiveStatsBinding.pager.unregisterOnPageChangeCallback(onPageChangeListener)
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
