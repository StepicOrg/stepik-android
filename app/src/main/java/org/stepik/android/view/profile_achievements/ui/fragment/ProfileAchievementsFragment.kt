package org.stepik.android.view.profile_achievements.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.error_no_connection_with_button_small.*
import kotlinx.android.synthetic.main.fragment_profile_achievements.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.core.ScreenManager
import org.stepik.android.domain.achievement.model.AchievementItem
import org.stepik.android.presentation.achievement.AchievementsView
import org.stepik.android.presentation.profile_achievements.ProfileAchievementsPresenter
import org.stepik.android.view.achievement.ui.adapter.delegate.AchievementTileAdapterDelegate
import org.stepik.android.view.achievement.ui.dialog.AchievementDetailsDialog
import org.stepik.android.view.achievement.ui.resolver.AchievementResourceResolver
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter
import ru.nobird.android.view.base.ui.extension.argument
import ru.nobird.android.view.base.ui.extension.showIfNotExists
import javax.inject.Inject

class ProfileAchievementsFragment : Fragment(), AchievementsView {
    companion object {
        private const val MAX_ACHIEVEMENTS_TO_DISPLAY = 6

        fun newInstance(userId: Long): Fragment =
            ProfileAchievementsFragment()
                .apply {
                    this.userId = userId
                }
    }

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    internal lateinit var screenManager: ScreenManager

    @Inject
    internal lateinit var achievementResourceResolver: AchievementResourceResolver

    private var userId: Long by argument()

    private lateinit var achievementsPresenter: ProfileAchievementsPresenter
    private lateinit var viewStateDelegate: ViewStateDelegate<AchievementsView.State>
    private lateinit var achievementsAdapter: DefaultDelegateAdapter<AchievementItem>

    private var achievementsToDisplay = 0
    private var isMyProfile = false
    private var profileId = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        achievementsToDisplay = resources.getInteger(R.integer.achievements_to_display)
        injectComponent()

        achievementsPresenter = ViewModelProviders
            .of(this, viewModelFactory)
            .get(ProfileAchievementsPresenter::class.java)
        savedInstanceState
            ?.let(achievementsPresenter::onRestoreInstanceState)

        achievementsAdapter = DefaultDelegateAdapter()
        achievementsAdapter += AchievementTileAdapterDelegate(achievementResourceResolver, ::onAchievementClicked)
    }

    private fun injectComponent() {
        App.componentManager()
            .profileComponent(userId)
            .inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_profile_achievements, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewStateDelegate = ViewStateDelegate()
        viewStateDelegate.addState<AchievementsView.State.Idle>()
        viewStateDelegate.addState<AchievementsView.State.SilentLoading>()
        viewStateDelegate.addState<AchievementsView.State.Loading>(view, achievementsLoadingPlaceholder)
        viewStateDelegate.addState<AchievementsView.State.Error>(view, achievementsLoadingError)
        viewStateDelegate.addState<AchievementsView.State.AchievementsLoaded>(view, achievementsTilesContainer)
        viewStateDelegate.addState<AchievementsView.State.NoAchievements>()

        tryAgain.setOnClickListener { setDataToPresenter(forceUpdate = true) }
        achievementsTitle.setOnClickListener { screenManager.showAchievementsList(requireContext(), profileId, isMyProfile) }

        achievementsTilesContainer.layoutManager = GridLayoutManager(context, achievementsToDisplay)
        achievementsTilesContainer.isNestedScrollingEnabled = false
        achievementsTilesContainer.adapter = achievementsAdapter
        initAchievementsPlaceholders()

        setDataToPresenter()
    }

    private fun setDataToPresenter(forceUpdate: Boolean = false) {
        achievementsPresenter.showAchievementsForUser(MAX_ACHIEVEMENTS_TO_DISPLAY, forceUpdate)
    }

    private fun initAchievementsPlaceholders() {
        for (i in 0 until achievementsToDisplay) {
            val view = layoutInflater.inflate(R.layout.view_achievement_tile_placeholder, achievementsLoadingPlaceholder, false)
            view.layoutParams = (view.layoutParams as LinearLayout.LayoutParams).apply {
                weight = 1f
                width = 0
            }
            achievementsLoadingPlaceholder.addView(view)
        }
    }

    override fun onStart() {
        super.onStart()

        achievementsPresenter.attachView(this)
    }

    override fun onStop() {
        achievementsPresenter.detachView(this)
        super.onStop()
    }

    private fun onAchievementClicked(item: AchievementItem) {
        AchievementDetailsDialog
            .newInstance(item, canShareAchievement = isMyProfile)
            .showIfNotExists(childFragmentManager, AchievementDetailsDialog.TAG)
    }

    override fun setState(state: AchievementsView.State) {
        viewStateDelegate.switchState(state)

        when (state) {
            is AchievementsView.State.Loading -> {
                profileId = state.userId
                isMyProfile = state.isMyProfile
            }
            is AchievementsView.State.AchievementsLoaded -> {
                achievementsAdapter.items = state.achievements.take(achievementsToDisplay)
                profileId = state.userId
                isMyProfile = state.isMyProfile
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        achievementsPresenter.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }
}