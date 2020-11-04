package org.stepic.droid.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.home_streak_view.*
import org.stepic.droid.R
import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.base.App
import org.stepic.droid.base.FragmentBase
import org.stepic.droid.core.presenters.HomeStreakPresenter
import org.stepic.droid.core.presenters.contracts.HomeStreakView
import org.stepic.droid.ui.util.initCenteredToolbar
import org.stepic.droid.util.commitNow
import org.stepik.android.view.course_list.ui.fragment.CourseListPopularFragment
import org.stepik.android.view.course_list.ui.fragment.CourseListUserHorizontalFragment
import org.stepik.android.view.course_list.ui.fragment.CourseListVisitedHorizontalFragment
import org.stepik.android.view.fast_continue.ui.fragment.FastContinueFragment
import javax.inject.Inject

class HomeFragment : FragmentBase(), HomeStreakView {
    companion object {
        const val TAG = "HomeFragment"

        fun newInstance(): HomeFragment = HomeFragment()
        private const val fastContinueTag = "fastContinueTag"
    }

    @Inject
    lateinit var homeStreakPresenter: HomeStreakPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        analytic.reportAmplitudeEvent(AmplitudeAnalytic.Home.HOME_SCREEN_OPENED)
    }

    override fun injectComponent() {
        App.component()
            .homeComponentBuilder()
            .build()
            .inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_home, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        nullifyActivityBackground()
        super.onViewCreated(view, savedInstanceState)
        initCenteredToolbar(R.string.home_title)

        if (savedInstanceState == null) {
            childFragmentManager.commitNow {
                add(R.id.homeMainContainer, FastContinueFragment.newInstance(), fastContinueTag)
                add(R.id.homeMainContainer, CourseListUserHorizontalFragment.newInstance())
                add(R.id.homeMainContainer, CourseListVisitedHorizontalFragment.newInstance())
                add(R.id.homeMainContainer, CourseListPopularFragment.newInstance())
            }
        }

        homeStreakPresenter.attachView(this)
        homeStreakPresenter.onNeedShowStreak()
    }

    override fun onDestroyView() {
        homeStreakPresenter.detachView(this)
        super.onDestroyView()
    }

    override fun showStreak(streak: Int) {
        streakCounter.text = streak.toString()

        val daysPlural = resources.getQuantityString(R.plurals.day_number, streak)
        val days = "$streak $daysPlural"

        streakText.text = textResolver.fromHtml(getString(R.string.home_streak_counter_text, days))
        homeStreak.isVisible = true
    }

    override fun onEmptyStreak() {
        homeStreak.isVisible = false
    }
}
