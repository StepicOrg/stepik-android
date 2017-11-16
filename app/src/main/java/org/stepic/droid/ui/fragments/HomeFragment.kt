package org.stepic.droid.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.home_streak_view.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.base.FragmentBase
import org.stepic.droid.core.presenters.HomeStreakPresenter
import org.stepic.droid.core.presenters.contracts.HomeStreakView
import org.stepic.droid.fonts.FontType
import org.stepic.droid.model.CoursesCarouselInfoConstants
import org.stepic.droid.ui.util.changeVisibility
import org.stepic.droid.ui.util.initCenteredToolbar
import uk.co.chrisjenx.calligraphy.CalligraphyTypefaceSpan
import uk.co.chrisjenx.calligraphy.TypefaceUtils
import javax.inject.Inject

class HomeFragment : FragmentBase(), HomeStreakView {
    companion object {
        fun newInstance(): HomeFragment = HomeFragment()
        private const val fastContinueTag = "fastContinueTag"
    }

    @Inject
    lateinit var homeStreakPresenter: HomeStreakPresenter

    private val regularFontSpan by lazy {
        CalligraphyTypefaceSpan(TypefaceUtils.load(context.assets, fontsProvider.provideFontPath(FontType.regular)))
    }

    override fun injectComponent() {
        App.component().inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater?.inflate(R.layout.fragment_home, container, false)

    @SuppressLint("CommitTransaction")
    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
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
    }

    override fun onDestroyView() {
        homeStreakPresenter.detachView(this)
        super.onDestroyView()
    }

    override fun showStreak(streak: Int) {
        val needShow = streak > 0
        if (needShow) {
            streakCounter.text = streak.toString()

            val daysPlural = resources.getQuantityString(R.plurals.day_number, streak)
            val daysSpannable = SpannableString("$streak $daysPlural")
            daysSpannable.setSpan(regularFontSpan, 0, daysSpannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            val streakTextSpannableBuilder = SpannableStringBuilder(getString(R.string.home_streak_counter_prefix))
            streakTextSpannableBuilder.append(daysSpannable)
            streakTextSpannableBuilder.append(getString(R.string.home_streak_counter_suffix))

            streakText.text = streakTextSpannableBuilder
        }
        homeStreak.changeVisibility(needShow)
    }
}
