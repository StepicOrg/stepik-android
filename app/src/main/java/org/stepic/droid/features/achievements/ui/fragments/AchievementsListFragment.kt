package org.stepic.droid.features.achievements.ui.fragments

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.base.FragmentBase
import org.stepic.droid.features.achievements.presenters.AchievementsPresenter
import org.stepic.droid.features.achievements.presenters.AchievementsView
import org.stepic.droid.features.achievements.ui.adapters.AchievementsAdapter
import org.stepic.droid.features.achievements.ui.adapters.BaseAchievementsAdapter
import org.stepic.droid.model.achievements.AchievementFlatItem
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

    private lateinit var recycler: RecyclerView

    override fun injectComponent() {
        App
                .component()
                .profileComponentBuilder()
                .build()
                .inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        recycler = RecyclerView(context)
        recycler.layoutManager = LinearLayoutManager(context)
        recycler.adapter = AchievementsAdapter()

        val divider = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        divider.setDrawable(ContextCompat.getDrawable(context, R.drawable.list_divider_h))
        recycler.addItemDecoration(divider)

        achievementsPresenter.attachView(this)
        achievementsPresenter.showAchievementsForUser(arguments?.getLong(USER_ID_KEY) ?: 0)

        return recycler
    }

    override fun showAchievements(achievements: List<AchievementFlatItem>) {
        (recycler.adapter as BaseAchievementsAdapter).addAchievements(achievements)
    }

    override fun onDestroyView() {
        achievementsPresenter.detachView(this)
        super.onDestroyView()
    }
}