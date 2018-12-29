package org.stepic.droid.adaptive.ui.adapters

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v7.content.res.AppCompatResources
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.adaptive_rating_item.view.*
import org.stepic.droid.R
import org.stepik.android.model.adaptive.RatingItem
import org.stepic.droid.preferences.SharedPreferenceHelper

class AdaptiveRatingAdapter (
        context: Context,
        sharedPreferenceHelper: SharedPreferenceHelper
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private companion object {
        private const val RATING_ITEM_VIEW_TYPE = 1
        private const val SEPARATOR_VIEW_TYPE = 2

        private const val SEPARATOR = -1L

        @JvmStatic
        private fun isRatingGap(current: RatingItem, next: RatingItem) =
                current.rank + 1 != next.rank

        @JvmStatic
        private fun isNotSeparatorStub(item: RatingItem) =
                item.user != SEPARATOR
    }

    private val profileId = sharedPreferenceHelper.profile?.id ?: 0

    private val leaderIconDrawable: Drawable? = AppCompatResources.getDrawable(context, R.drawable.ic_crown)?.apply {
        DrawableCompat.setTint(this, ContextCompat.getColor(context, R.color.adaptive_color_yellow))
    }

    private val leaderIconDrawableSelected: Drawable? = AppCompatResources.getDrawable(context, R.drawable.ic_crown)?.apply {
        DrawableCompat.setTint(this, ContextCompat.getColor(context,  android.R.color.white))
    }

    private val items = ArrayList<RatingItem>()

    override fun getItemViewType(position: Int) =
            if (isNotSeparatorStub(items[position])) {
                RATING_ITEM_VIEW_TYPE
            } else {
                SEPARATOR_VIEW_TYPE
            }

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            when(viewType) {
                RATING_ITEM_VIEW_TYPE -> RatingViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.adaptive_rating_item, parent, false))
                SEPARATOR_VIEW_TYPE -> SeparatorViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.adaptive_ranks_separator, parent, false))
                else -> throw IllegalStateException("Unknown view type $viewType")
            }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            RATING_ITEM_VIEW_TYPE -> {
                (holder as RatingViewHolder).let {
                    it.rank.text = items[position].rank.toString()
                    it.exp.text = items[position].exp.toString()
                    it.name.text = items[position].name

                    it.root.isSelected = profileId == items[position].user

                    if (items[position].rank == 1) {
                        it.icon.setImageDrawable(if (profileId == items[position].user) {
                            leaderIconDrawableSelected
                        } else {
                            leaderIconDrawable
                        })

                        it.icon.visibility = View.VISIBLE
                        it.rank.visibility = View.GONE
                    } else {
                        it.icon.visibility = View.GONE
                        it.rank.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    fun set(data: Collection<RatingItem>) {
        items.clear()
        items.addAll(data)
        addSeparator()
        notifyDataSetChanged()
    }

    private fun addSeparator() {
        (items.size - 2 downTo 0)
                .filter { isRatingGap(items[it], items[it + 1]) && isNotSeparatorStub(items[it]) && isNotSeparatorStub(items[it + 1]) }
                .forEach { items.add(it + 1, RatingItem(0, "", 0, SEPARATOR)) }
    }

    class RatingViewHolder(val root: View) : RecyclerView.ViewHolder(root) {
        val icon: ImageView = root.icon
        val rank: TextView = root.rank
        val exp: TextView = root.exp
        val name: TextView = root.name
    }

    class SeparatorViewHolder(view: View) : RecyclerView.ViewHolder(view)
}