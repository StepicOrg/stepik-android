package org.stepic.droid.features.stories.ui.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.view_story.view.*
import org.stepic.droid.R
import org.stepic.droid.ui.util.RoundedBitmapImageViewTarget
import org.stepic.droid.ui.util.changeVisibility
import ru.nobird.android.stories.model.Story

class StoriesAdapter(private val onStoryClicked: (Story, Int) -> Unit) : RecyclerView.Adapter<StoriesAdapter.StoryViewHolder>() {
    var stories: List<Story> = emptyList()
        private set

    var viewedStoryIds: Set<Long> = emptySet()
        private set

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
            LayoutInflater.from(parent.context).inflate(R.layout.view_story, parent, false)
    )


    override fun getItemCount() = stories.size

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class StoryViewHolder(root: View) : RecyclerView.ViewHolder(root) {
        private val cover = root.storyCover
        private val title = root.storyTitle
        private val activeStoryMarker = root.activeStoryMarker

        private val coverTarget = RoundedBitmapImageViewTarget(1f, cover)

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

            Glide.with(cover.context)
                    .load(story.cover)
                    .asBitmap()
                    .placeholder(R.drawable.general_placeholder)
                    .centerCrop()
                    .into(coverTarget)

            activeStoryMarker.changeVisibility(!viewedStoryIds.contains(story.id))
        }
    }
}