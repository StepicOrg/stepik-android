package org.stepic.droid.features.achievements.ui.fragments

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.error_no_connection_with_button.*
import kotlinx.android.synthetic.main.fragment_achievements_list.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.base.FragmentBase
import org.stepic.droid.features.achievements.presenters.AchievementsPresenter
import org.stepic.droid.features.achievements.presenters.AchievementsView
import org.stepic.droid.features.achievements.ui.adapters.AchievementsAdapter
import org.stepic.droid.features.achievements.ui.adapters.BaseAchievementsAdapter
import org.stepic.droid.features.achievements.ui.dialogs.AchievementDetailsDialog
import org.stepic.droid.model.achievements.AchievementFlatItem
import org.stepic.droid.ui.util.changeVisibility
import org.stepic.droid.ui.util.initCenteredToolbar
import org.stepic.droid.ui.util.setHeight
import javax.inject.Inject

class AchievementsListFragment: FragmentBase(), AchievementsView {
    companion object {
        const val USER_ID_KEY = "user_id"

        fun newInstance(userId: Long) =
            AchievementsListFragment().apply {
                arguments = Bundle(1).apply { putLong(USER_ID_KEY, userId) }
            }
    }

    @Inject
    lateinit var achievementsPresenter: AchievementsPresenter

    override fun injectComponent() {
        App
                .component()
                .profileComponentBuilder()
                .build()
                .inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_achievements_list, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initPlaceholders()

        initCenteredToolbar(R.string.achievements_title, showHomeButton = true)

        recycler.layoutManager = LinearLayoutManager(context)
        recycler.adapter = AchievementsAdapter().apply { onAchievementItemClick = {
            AchievementDetailsDialog.newInstance(it).show(childFragmentManager, AchievementDetailsDialog.TAG)
        }}

        val divider = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        divider.setDrawable(ContextCompat.getDrawable(context, R.drawable.list_divider_h))
        recycler.addItemDecoration(divider)

        achievementsPresenter.attachView(this)
        fetchAchievements()

        tryAgain.setOnClickListener { fetchAchievements(true) }
    }

    private fun initPlaceholders() {
        val itemHeight = context.resources.getDimension(R.dimen.achievement_tile_height)
        val screenHeight = context.resources.displayMetrics.heightPixels

        for (i in 0..(screenHeight / itemHeight).toInt()) {
            progress.addView(layoutInflater.inflate(R.layout.view_achievement_item_placeholder, progress, false))
            val stroke = layoutInflater.inflate(R.layout.view_stroke, progress, false)
            stroke.setBackgroundResource(R.drawable.list_divider_h)
            stroke.setHeight(1)
            progress.addView(stroke)
        }
    }

    private fun fetchAchievements(force: Boolean = false) {
        achievementsPresenter.showAchievementsForUser(arguments?.getLong(USER_ID_KEY) ?: 0, force = force)
    }

    override fun showAchievements(achievements: List<AchievementFlatItem>) {
        recycler.changeVisibility(true)
        progress.changeVisibility(false)
        error.changeVisibility(false)
        (recycler?.adapter as? BaseAchievementsAdapter)?.achievements = achievements
    }

    override fun onAchievementsLoadingError() {
        recycler.changeVisibility(false)
        progress.changeVisibility(false)
        error.changeVisibility(true)
    }

    override fun onAchievementsLoading() {
        recycler.changeVisibility(false)
        progress.changeVisibility(true)
        error.changeVisibility(false)
    }

    override fun onDestroyView() {
        achievementsPresenter.detachView(this)
        super.onDestroyView()
    }
}