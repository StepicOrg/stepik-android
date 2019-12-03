package org.stepik.android.view.achievement.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.error_no_connection_with_button.*
import kotlinx.android.synthetic.main.fragment_achievements_list.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepik.android.presentation.achievement.AchievementsPresenter
import org.stepik.android.presentation.achievement.AchievementsView
import org.stepic.droid.features.achievements.ui.adapters.AchievementsAdapter
import org.stepic.droid.features.achievements.ui.adapters.BaseAchievementsAdapter
import org.stepic.droid.features.achievements.util.AchievementResourceResolver
import org.stepic.droid.model.AchievementFlatItem
import org.stepik.android.view.achievement.ui.dialog.AchievementDetailsDialog
import org.stepic.droid.ui.util.initCenteredToolbar
import org.stepic.droid.ui.util.setHeight
import org.stepik.android.view.achievement.ui.adapter.delegate.AchievementAdapterDelegate
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter
import ru.nobird.android.view.base.ui.extension.argument
import ru.nobird.android.view.base.ui.extension.showIfNotExists
import javax.inject.Inject

class AchievementsListFragment: Fragment(), AchievementsView {
    companion object {
        fun newInstance(userId: Long, isMyProfile: Boolean) =
            AchievementsListFragment().apply {
                this.userId = userId
                this.isMyProfile = isMyProfile
            }
    }

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    internal lateinit var achievementResourceResolver: AchievementResourceResolver

    private lateinit var achievementsPresenter: AchievementsPresenter
    private lateinit var viewStateDelegate: ViewStateDelegate<AchievementsView.State>

    private lateinit var achievementsAdapter: DefaultDelegateAdapter<AchievementFlatItem>

    private var userId: Long by argument()
    private var isMyProfile: Boolean by argument()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectComponent()

        achievementsPresenter = ViewModelProviders
            .of(this, viewModelFactory)
            .get(AchievementsPresenter::class.java)

        achievementsAdapter = DefaultDelegateAdapter()
        achievementsAdapter += AchievementAdapterDelegate(achievementResourceResolver, ::onAchievementClicked)
    }

    private fun injectComponent() {
        App
            .componentManager()
            .profileComponent(userId)
            .inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_achievements_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context = requireContext()

        viewStateDelegate = ViewStateDelegate()
        viewStateDelegate.addState<AchievementsView.State.Idle>(progress)
        viewStateDelegate.addState<AchievementsView.State.Loading>(progress)
        viewStateDelegate.addState<AchievementsView.State.Error>(error)
        viewStateDelegate.addState<AchievementsView.State.AchievementsLoaded>(recycler)

        initPlaceholders()

        initCenteredToolbar(R.string.achievements_title, showHomeButton = true)

        recycler.layoutManager = LinearLayoutManager(context)
        recycler.adapter = AchievementsAdapter().apply { onAchievementItemClick = {
            AchievementDetailsDialog
                .newInstance(it, isMyProfile)
                .showIfNotExists(childFragmentManager, AchievementDetailsDialog.TAG)
        }}

        val divider = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        divider.setDrawable(ContextCompat.getDrawable(context, R.drawable.list_divider_h)!!)
        recycler.addItemDecoration(divider)

        achievementsPresenter.attachView(this)
        fetchAchievements()

        tryAgain.setOnClickListener { fetchAchievements(true) }
    }

    private fun initPlaceholders() {
        val itemHeight = resources.getDimension(R.dimen.achievement_tile_height)
        val screenHeight = resources.displayMetrics.heightPixels

        for (i in 0..(screenHeight / itemHeight).toInt()) {
            progress.addView(layoutInflater.inflate(R.layout.view_achievement_item_placeholder, progress, false))
            val stroke = layoutInflater.inflate(R.layout.view_stroke, progress, false)
            stroke.setBackgroundResource(R.drawable.list_divider_h)
            stroke.setHeight(1)
            progress.addView(stroke)
        }
    }

    private fun onAchievementClicked(item: AchievementFlatItem) {
        AchievementDetailsDialog
            .newInstance(item, isMyProfile)
            .showIfNotExists(childFragmentManager, AchievementDetailsDialog.TAG)
    }

    private fun fetchAchievements(forceUpdate: Boolean = false) {
        achievementsPresenter.showAchievementsForUser(userId, forceUpdate = forceUpdate)
    }

    override fun setState(state: AchievementsView.State) {
        viewStateDelegate.switchState(state)

        if (state is AchievementsView.State.AchievementsLoaded) {
            (recycler.adapter as? BaseAchievementsAdapter)?.achievements = state.achievements
        }
    }

    override fun onDestroyView() {
        achievementsPresenter.detachView(this)
        super.onDestroyView()
    }
}