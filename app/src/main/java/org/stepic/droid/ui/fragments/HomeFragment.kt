package org.stepic.droid.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.home_streak_view.*
import org.stepic.droid.R
import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.base.FragmentBase
import org.stepic.droid.core.presenters.HomeStreakPresenter
import org.stepic.droid.core.presenters.contracts.HomeStreakView
import org.stepic.droid.model.CoursesCarouselInfoConstants
import org.stepic.droid.ui.util.initCenteredToolbar
import org.stepic.droid.util.commitNow
import org.stepik.android.view.course_list.ui.fragment.CourseListPopularFragment
import org.stepik.android.view.course_list.ui.fragment.CourseListUserHorizontalFragment
import javax.inject.Inject

class HomeFragment : FragmentBase(), HomeStreakView {
    companion object {
        fun newInstance(): HomeFragment = HomeFragment()
        private const val fastContinueTag = "fastContinueTag"
    }

    @Inject
    lateinit var homeStreakPresenter: HomeStreakPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        analytic.reportAmplitudeEvent(AmplitudeAnalytic.Home.HOME_SCREEN_OPENED)
        analytic.reportEvent(Analytic.Home.HOME_SCREEN_OPENED)
    }

    override fun injectComponent() {
        App.component()
            .homeComponentBuilder()
            .build()
            .inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_home, container, false)

    @SuppressLint("CommitTransaction")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        nullifyActivityBackground()
        super.onViewCreated(view, savedInstanceState)
        initCenteredToolbar(R.string.home_title)

        if (savedInstanceState == null) {
            childFragmentManager.commitNow {
                add(R.id.homeFastContinueContainer, FastContinueFragment.newInstance(), fastContinueTag)
                add(R.id.popularCoursesContainer, CourseListPopularFragment.newInstance())
                add(R.id.userCoursesContainer, CourseListUserHorizontalFragment.newInstance())
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
