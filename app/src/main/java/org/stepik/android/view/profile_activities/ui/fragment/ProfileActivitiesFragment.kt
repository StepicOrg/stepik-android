package org.stepik.android.view.profile_activities.ui.fragment

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import ru.nobird.android.view.base.ui.extension.argument
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.error_no_connection_with_button_small.*
import kotlinx.android.synthetic.main.fragment_profile_activities.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepik.android.presentation.profile_activities.ProfileActivitiesPresenter
import org.stepik.android.presentation.profile_activities.ProfileActivitiesView
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import javax.inject.Inject

class ProfileActivitiesFragment : Fragment(R.layout.fragment_profile_activities), ProfileActivitiesView {
    companion object {
        fun newInstance(userId: Long): Fragment =
            ProfileActivitiesFragment()
                .apply {
                    this.userId = userId
                }
    }

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private var userId by argument<Long>()

    private lateinit var profileActivitiesPresenter: ProfileActivitiesPresenter

    private lateinit var viewStateDelegate: ViewStateDelegate<ProfileActivitiesView.State>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        injectComponent()

        profileActivitiesPresenter = ViewModelProviders
            .of(this, viewModelFactory)
            .get(ProfileActivitiesPresenter::class.java)
    }

    private fun injectComponent() {
        App.componentManager()
            .profileComponent(userId)
            .inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewStateDelegate = ViewStateDelegate()
        viewStateDelegate.addState<ProfileActivitiesView.State.Idle>()
        viewStateDelegate.addState<ProfileActivitiesView.State.SilentLoading>()
        viewStateDelegate.addState<ProfileActivitiesView.State.Empty>()
        viewStateDelegate.addState<ProfileActivitiesView.State.Loading>(view, streakLoadingPlaceholder)
        viewStateDelegate.addState<ProfileActivitiesView.State.Error>(view, streakLoadingError)
        viewStateDelegate.addState<ProfileActivitiesView.State.Content>(view, streakContainer)

        setDataToPresenter()
        tryAgain.setOnClickListener { setDataToPresenter(forceUpdate = true) }
    }

    private fun setDataToPresenter(forceUpdate: Boolean = false) {
        profileActivitiesPresenter.fetchUserActivities(forceUpdate)
    }

    override fun onStart() {
        super.onStart()

        profileActivitiesPresenter.attachView(this)
    }

    override fun onStop() {
        profileActivitiesPresenter.detachView(this)
        super.onStop()
    }

    override fun setState(state: ProfileActivitiesView.State) {
        viewStateDelegate.switchState(state)

        if (state is ProfileActivitiesView.State.Content) {
            with(state.profileActivitiesData) {
                @ColorRes
                val streakTintColorRes =
                    if (isSolvedToday) {
                        R.color.color_overlay_green
                    } else {
                        R.color.color_overlay_yellow
                    }

                currentStreak.supportCompoundDrawablesTintList = ColorStateList
                    .valueOf(ContextCompat.getColor(requireContext(), streakTintColorRes))

                currentStreakCount.text = streak
                    .takeIf { it > 0 }
                    ?.toString()
                    .orEmpty()

                @StringRes
                val currentStreakRes =
                    when {
                        isSolvedToday ->
                            R.string.profile_activities_current_streak_active

                        streak > 0 ->
                            R.string.profile_activities_current_streak_continue

                        else ->
                            R.string.profile_activities_current_streak_start
                    }
                currentStreak.setText(currentStreakRes)

                maxStreakCount.text = maxStreak.toString()
            }
        }
    }
}