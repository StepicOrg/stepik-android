package org.stepic.droid.features.stories.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import kotlinx.android.synthetic.main.view_story_item.view.*
import org.stepic.droid.R
import ru.nobird.android.stories.model.Story
import kotlin.properties.Delegates

class StoriesAdapter(
    private val onStoryClicked: (Story, Int) -> Unit
) : RecyclerView.Adapter<StoriesAdapter.StoryViewHolder>() {

    var stories: List<Story> = emptyList()
        private set

    var viewedStoryIds: Set<Long> = emptySet()
        private set

    var selected: Int by Delegates.observable(-1) { _, old, new ->
        notifyItemChanged(old)
        notifyItemChanged(new)
    }

    fun setData(stories: List<Story>, viewedStoryIds: Set<Long>) {
        var updateNeeded = false
        if (this.stories != stories) {
            this.stories = stories
            updateNeeded = true
        }

        if (this.viewedStoryIds != viewedStoryIds) {
            if (updateNeeded) {
                this.viewedStoryIds = viewedStoryIds
            } else {
                val diff = viewedStoryIds - this.viewedStoryIds
                this.viewedStoryIds = viewedStoryIds
                diff.forEach { storyId ->
                    val index = stories.indexOfFirst { it.id == storyId }
                    if (index != -1) {
                        notifyItemChanged(index)
                    }
                }
            }
        }

        if (updateNeeded) {
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = StoryViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.view_story_item, parent, false)
    )

    override fun getItemCount() = stories.size

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class StoryViewHolder(root: View) : RecyclerView.ViewHolder(root) {
        val cover: ShapeableImageView = root.storyCover
        private val title = root.storyTitle
        private val activeStoryMarker = root.activeStoryMarker

        init {
            root.setOnClickListener {
                if (adapterPosition in stories.indices) {
                    onStoryClicked(stories[adapterPosition], adapterPosition)
                }
            }
        }

        fun bind(position: Int) {
            val story = stories[position]
            title.text = story.title

            Glide.with(itemView.context)
                    .asBitmap()
                    .load(story.cover)
                    .placeholder(R.drawable.ic_general_placeholder_dark)
                    .centerCrop()
                    .into(cover)

            activeStoryMarker.isGone = story.id in viewedStoryIds
            itemView.isInvisible = position == selected
        }
    }
}