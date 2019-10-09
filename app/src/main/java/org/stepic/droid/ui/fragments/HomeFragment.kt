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
import org.stepic.droid.base.App
import org.stepic.droid.base.FragmentBase
import org.stepic.droid.core.presenters.HomeStreakPresenter
import org.stepic.droid.core.presenters.contracts.HomeStreakView
import org.stepic.droid.model.CoursesCarouselInfoConstants
import org.stepic.droid.ui.util.initCenteredToolbar
import javax.inject.Inject

class HomeFragment : FragmentBase(), HomeStreakView {
    companion object {
        fun newInstance(): HomeFragment = HomeFragment()
        private const val fastContinueTag = "fastContinueTag"
    }

    @Inject
    lateinit var homeStreakPresenter: HomeStreakPresenter

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
            childFragmentManager
                .beginTransaction() //false positive Lint: ... should completed with commit()
                .add(R.id.homeFastContinueContainer, FastContinueFragment.newInstance(), fastContinueTag)
                .commitNow()


            myCoursesView.setCourseCarouselInfo(CoursesCarouselInfoConstants.myCourses)
            popularCoursesView.setCourseCarouselInfo(CoursesCarouselInfoConstants.popular)
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
