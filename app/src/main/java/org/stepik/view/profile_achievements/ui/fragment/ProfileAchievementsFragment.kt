package org.stepik.view.profile_achievements.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ru.nobird.android.view.base.ui.extension.argument
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepik.android.presentation.achievement.AchievementsPresenter
import org.stepik.android.presentation.achievement.AchievementsView
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import javax.inject.Inject

class ProfileAchievementsFragment : Fragment(), AchievementsView {
    companion object {
        fun newInstance(userId: Long): Fragment =
            ProfileAchievementsFragment()
                .apply {
                    this.userId = userId
                }
    }

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private var userId: Long by argument()

    private lateinit var achievementsPresenter: AchievementsPresenter

    private lateinit var viewStateDelegate: ViewStateDelegate<AchievementsView.State>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        injectComponent()

        achievementsPresenter = ViewModelProviders
            .of(this, viewModelFactory)
            .get(AchievementsPresenter::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_profile_achievements, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewStateDelegate = ViewStateDelegate()

    }

    private fun injectComponent() {
        App.componentManager()
            .profileComponent(userId)
            .inject(this)
    }

    override fun onStart() {
        super.onStart()

        achievementsPresenter.attachView(this)
    }

    override fun onStop() {
        achievementsPresenter.detachView(this)
        super.onStop()
    }

    override fun setState(state: AchievementsView.State) {
        when (state) {

        }
    }
}