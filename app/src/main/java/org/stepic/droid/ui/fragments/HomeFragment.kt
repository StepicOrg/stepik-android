package org.stepic.droid.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.home_streak_view.*
import kotlinx.android.synthetic.main.view_centered_toolbar.*
import org.stepic.droid.R
import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.base.App
import org.stepic.droid.base.FragmentBase
import org.stepic.droid.configuration.RemoteConfig
import org.stepic.droid.core.presenters.HomeStreakPresenter
import org.stepic.droid.core.presenters.contracts.HomeStreakView
import org.stepic.droid.util.commitNow
import org.stepik.android.view.course_list.ui.fragment.CourseListPopularFragment
import org.stepik.android.view.course_list.ui.fragment.CourseListUserHorizontalFragment
import org.stepik.android.view.course_list.ui.fragment.CourseListUserHorizontalNewHomeFragment
import org.stepik.android.view.course_list.ui.fragment.CourseListVisitedHorizontalFragment
import org.stepik.android.view.fast_continue.ui.fragment.FastContinueFragment
import org.stepik.android.view.fast_continue.ui.fragment.FastContinueNewHomeFragment
import org.stepik.android.view.stories.ui.fragment.StoriesFragment
import ru.nobird.android.stories.transition.SharedTransitionsManager
import ru.nobird.android.stories.ui.delegate.SharedTransitionContainerDelegate
import javax.inject.Inject

class HomeFragment : FragmentBase(), HomeStreakView, FastContinueNewHomeFragment.Callback {
    companion object {
        const val TAG = "HomeFragment"
        const val HOME_DEEPLINK_STORY_KEY = "home_deeplink_story_key"

        fun newInstance(): HomeFragment = HomeFragment()
        private const val fastContinueTag = "fastContinueTag"
    }

    @Inject
    lateinit var homeStreakPresenter: HomeStreakPresenter

    @Inject
    lateinit var remoteConfig: FirebaseRemoteConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        centeredToolbarTitle.setText(R.string.home_title)

        if (savedInstanceState == null) {
            setupFragments(remoteConfig.getBoolean(RemoteConfig.IS_NEW_HOME_SCREEN_ENABLED))
        }

        appBarLayout.isVisible = !remoteConfig.getBoolean(RemoteConfig.IS_NEW_HOME_SCREEN_ENABLED)

        homeStreakPresenter.attachView(this)
        homeStreakPresenter.onNeedShowStreak()
    }

    override fun onStart() {
        super.onStart()
        if (remoteConfig.getBoolean(RemoteConfig.IS_NEW_HOME_SCREEN_ENABLED)) {
            SharedTransitionsManager.registerTransitionDelegate(HOME_DEEPLINK_STORY_KEY, object :
                SharedTransitionContainerDelegate {
                override fun getSharedView(position: Int): View? =
                    storyDeepLinkMockView

                override fun onPositionChanged(position: Int) {}
            })
        }
    }

    override fun onStop() {
        if (remoteConfig.getBoolean(RemoteConfig.IS_NEW_HOME_SCREEN_ENABLED)) {
            SharedTransitionsManager.unregisterTransitionDelegate(HOME_DEEPLINK_STORY_KEY)
        }
        super.onStop()
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

    private fun setupFragments(isNewHomeScreenEnabled: Boolean) {
        if (isNewHomeScreenEnabled) {
            childFragmentManager.commitNow {
                add(R.id.homeMainContainer, StoriesFragment.newInstance())
                add(R.id.homeMainContainer, CourseListUserHorizontalNewHomeFragment.newInstance())
                add(R.id.homeMainContainer, CourseListVisitedHorizontalFragment.newInstance())
                add(R.id.homeMainContainer, CourseListPopularFragment.newInstance())
                add(R.id.fastContinueContainer, FastContinueNewHomeFragment.newInstance())
            }
        } else {
            childFragmentManager.commitNow {
                add(R.id.homeMainContainer, FastContinueFragment.newInstance(), fastContinueTag)
                add(R.id.homeMainContainer, CourseListUserHorizontalFragment.newInstance())
                add(R.id.homeMainContainer, CourseListVisitedHorizontalFragment.newInstance())
                add(R.id.homeMainContainer, CourseListPopularFragment.newInstance())
            }
        }
    }

    override fun onFastContinueLoaded(isVisible: Boolean) {
        val padding = if (isVisible) {
            resources.getDimensionPixelOffset(R.dimen.fast_continue_widget_height)
        } else {
            0
        }
        homeNestedScrollView.setPadding(0, 0, 0, padding)
    }
}
