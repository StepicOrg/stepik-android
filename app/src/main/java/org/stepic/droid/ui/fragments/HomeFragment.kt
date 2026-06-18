package org.stepic.droid.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.findFragment
import dev.androidbroadcast.vbpd.viewBinding
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import org.stepic.droid.R
import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.base.App
import org.stepic.droid.base.FragmentBase
import org.stepic.droid.configuration.RemoteConfig
import org.stepic.droid.core.presenters.HomeStreakPresenter
import org.stepic.droid.core.presenters.contracts.HomeStreakView
import org.stepic.droid.databinding.FragmentHomeBinding
import org.stepic.droid.databinding.ItemBannerBinding
import org.stepic.droid.util.commitNow
import org.stepik.android.domain.banner.analytic.PromoBannerClickedAnalyticEvent
import org.stepik.android.domain.banner.analytic.PromoBannerSeen
import org.stepik.android.domain.banner.interactor.BannerInteractor
import org.stepik.android.domain.banner.model.Banner
import org.stepik.android.domain.home.interactor.HomeInteractor
import org.stepik.android.view.banner.mapper.BannerResourcesMapper
import org.stepik.android.view.banner.extension.bind
import org.stepik.android.view.banner.extension.handleItemClick
import org.stepik.android.view.course_list.ui.fragment.CourseListPopularFragment
import org.stepik.android.view.course_list.ui.fragment.CourseListUserHorizontalFragment
import org.stepik.android.view.course_list.ui.fragment.CourseListVisitedHorizontalFragment
import org.stepik.android.view.fast_continue.ui.fragment.FastContinueFragment
import org.stepik.android.view.fast_continue.ui.fragment.FastContinueNewHomeFragment
import org.stepik.android.view.learning_actions.ui.fragment.LearningActionsFragment
import org.stepik.android.view.stories.ui.fragment.StoriesFragment
import ru.nobird.android.stories.transition.SharedTransitionsManager
import ru.nobird.android.stories.ui.delegate.SharedTransitionContainerDelegate
import javax.inject.Inject
import kotlin.math.min

class HomeFragment : FragmentBase(), HomeStreakView, FastContinueNewHomeFragment.Callback {
    companion object {
        const val TAG = "HomeFragment"
        const val HOME_DEEPLINK_STORY_KEY = "home_deeplink_story_key"

        fun newInstance(): HomeFragment = HomeFragment()
        private const val fastContinueTag = "fastContinueTag"
    }

    private val homeBinding: FragmentHomeBinding by viewBinding(FragmentHomeBinding::bind)

    @Inject
    lateinit var homeStreakPresenter: HomeStreakPresenter

    @Inject
    lateinit var homeInteractor: HomeInteractor

    @Inject
    lateinit var remoteConfig: FirebaseRemoteConfig

    @Inject
    lateinit var bannerResourcesMapper: BannerResourcesMapper

    @Inject
    lateinit var bannerInteractor: BannerInteractor

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
        homeBinding.appBarLayout.centeredToolbarContainer.centeredToolbarTitle.setText(R.string.home_title)

        if (savedInstanceState == null) {
            setupFragments(remoteConfig.getBoolean(RemoteConfig.IS_NEW_HOME_SCREEN_ENABLED))
        }

        homeBinding.appBarLayout.root.isVisible = !remoteConfig.getBoolean(RemoteConfig.IS_NEW_HOME_SCREEN_ENABLED)

        homeStreakPresenter.attachView(this)
        homeStreakPresenter.onNeedShowStreak()

        homeBinding.homeMainContainer.post { setupBanners() }
    }

    override fun onStart() {
        super.onStart()
        if (remoteConfig.getBoolean(RemoteConfig.IS_NEW_HOME_SCREEN_ENABLED)) {
            SharedTransitionsManager.registerTransitionDelegate(HOME_DEEPLINK_STORY_KEY, object :
                SharedTransitionContainerDelegate {
                override fun getSharedView(position: Int): View? =
                    homeBinding.storyDeepLinkMockView

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
        homeBinding.homeStreak.streakCounter.text = streak.toString()

        val daysPlural = resources.getQuantityString(R.plurals.day_number, streak)
        val days = "$streak $daysPlural"

        homeBinding.homeStreak.streakText.text = textResolver.fromHtml(getString(R.string.home_streak_counter_text, days))
        homeBinding.homeStreak.root.isVisible = true
    }

    override fun onEmptyStreak() {
        homeBinding.homeStreak.root.isVisible = false
    }

    private fun setupFragments(isNewHomeScreenEnabled: Boolean) {
        if (isNewHomeScreenEnabled) {
            childFragmentManager.commitNow {
                add(R.id.homeMainContainer, StoriesFragment.newInstance())
                add(R.id.homeMainContainer, CourseListUserHorizontalFragment.newInstance())
                add(R.id.homeMainContainer, CourseListVisitedHorizontalFragment.newInstance())
                add(R.id.homeMainContainer, CourseListPopularFragment.newInstance())
                add(R.id.fastContinueContainer, FastContinueNewHomeFragment.newInstance())
            }
        } else {
            childFragmentManager.commitNow {
                add(R.id.homeMainContainer, FastContinueFragment.newInstance(), fastContinueTag)
                add(R.id.homeMainContainer, CourseListUserHorizontalFragment.newInstance())
                if (homeInteractor.isUserAuthorized()) {
                    add(R.id.homeMainContainer, LearningActionsFragment.newInstance())
                }
                add(R.id.homeMainContainer, CourseListVisitedHorizontalFragment.newInstance())
                add(R.id.homeMainContainer, CourseListPopularFragment.newInstance())
            }
        }
    }

    private fun setupBanners() {
        val banners = bannerInteractor
            .getBanners(Banner.Screen.HOME)
            .blockingGet()

        /**
         * Account for streak view and stories
         */
        val offset =
            if (remoteConfig.getBoolean(RemoteConfig.IS_NEW_HOME_SCREEN_ENABLED)) {
                2
            } else {
                1
            }

        banners.forEach { banner ->
            val binding = ItemBannerBinding.inflate(layoutInflater, homeBinding.homeMainContainer, false)

            binding.root.setOnClickListener {
                // TODO // Probably better to move into ViewTreeObserver
                analytic.report(PromoBannerSeen(banner))

                analytic.report(PromoBannerClickedAnalyticEvent(banner))
                binding.handleItemClick(banner, childFragmentManager)
            }

            binding.bind(banner, bannerResourcesMapper)

            val insertionIndex = min(banner.position + offset, homeBinding.homeMainContainer.childCount)
            val previousFragment = homeBinding.homeMainContainer.getChildAt(insertionIndex - 1).findFragment<Fragment>()

            homeBinding.homeMainContainer.addView(binding.root, insertionIndex)
            binding.root.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                val margin =
                    if (previousFragment is LearningActionsFragment) {
                        resources.getDimensionPixelOffset(R.dimen.course_list_side_padding)
                    } else {
                        0
                    }
                topMargin = margin
            }
        }
    }

    override fun onFastContinueLoaded(isVisible: Boolean) {
        val padding = if (isVisible) {
            resources.getDimensionPixelOffset(R.dimen.fast_continue_widget_height)
        } else {
            0
        }
        homeBinding.homeNestedScrollView.setPadding(0, 0, 0, padding)
    }
}
