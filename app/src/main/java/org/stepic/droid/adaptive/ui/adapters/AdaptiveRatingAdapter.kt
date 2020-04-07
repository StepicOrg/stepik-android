package org.stepic.droid.adaptive.ui.adapters

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.adaptive_rating_item.view.*
import org.stepic.droid.R
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepik.android.model.adaptive.RatingItem
import org.stepik.android.view.base.ui.extension.ColorExtensions

class AdaptiveRatingAdapter(
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

    private val leaderIconDrawableTint =
        ColorStateList.valueOf(ContextCompat.getColor(context, R.color.color_secondary))

    private val leaderIconDrawableSelectedTint =
        ColorStateList.valueOf(ContextCompat.getColor(context, R.color.color_on_secondary))

    private val items = ArrayList<RatingItem>()

    override fun getItemViewType(position: Int): Int =
        if (isNotSeparatorStub(items[position])) {
            RATING_ITEM_VIEW_TYPE
        } else {
            SEPARATOR_VIEW_TYPE
        }

    override fun getItemCount(): Int =
        items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        when(viewType) {
            RATING_ITEM_VIEW_TYPE ->
                RatingViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.adaptive_rating_item, parent, false))

            SEPARATOR_VIEW_TYPE ->
                SeparatorViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.adaptive_ranks_separator, parent, false))

            else ->
                throw IllegalStateException("Unknown view type $viewType")
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
                        val tint =
                            if (profileId == items[position].user) {
                                leaderIconDrawableSelectedTint
                            } else {
                                leaderIconDrawableTint
                            }
                        ImageViewCompat.setImageTintList(it.icon, tint)

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

    class SeparatorViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        init {
            val elevation = view.resources.getInteger(R.integer.highlighted_element_elevation)
            view.setBackgroundColor(ColorExtensions.colorSurfaceWithElevationOverlay(view.context, elevation, overrideLightTheme = true))
        }
    }
}