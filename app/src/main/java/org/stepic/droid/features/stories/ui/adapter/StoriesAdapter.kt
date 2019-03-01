package org.stepic.droid.features.stories.ui.adapter

import android.content.Context
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.support.v7.content.res.AppCompatResources
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.view_story_item.view.*
import org.stepic.droid.R
import org.stepic.droid.configuration.Config
import org.stepic.droid.ui.util.RoundedBitmapImageViewTarget
import org.stepic.droid.ui.util.changeVisibility
import org.stepic.droid.ui.util.toBitmap
import ru.nobird.android.stories.model.Story
import kotlin.properties.Delegates

class StoriesAdapter(
    private val context: Context,
    private val onStoryClicked: (Story, Int) -> Unit
) : RecyclerView.Adapter<StoriesAdapter.StoryViewHolder>() {
    private val coursePlaceholderDrawable by lazy {
        val resources = context.resources
        val size = resources.getDimension(R.dimen.stories_size).toInt()

        val drawable = AppCompatResources.getDrawable(context, R.drawable.ic_general_placeholder_dark)!!
        val bitmap = drawable.toBitmap(size, size)

        val circularBitmapDrawable = RoundedBitmapDrawableFactory.create(resources, bitmap)
        circularBitmapDrawable.cornerRadius = resources.getDimension(R.dimen.stories_default_corner_radius)
        return@lazy circularBitmapDrawable
    }

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
        val cover: ImageView = root.storyCover
        private val title = root.storyTitle
        private val activeStoryMarker = root.activeStoryMarker

        private val coverTarget = RoundedBitmapImageViewTarget(
                root.context.resources.getDimension(R.dimen.stories_default_corner_radius), cover)

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
                    .placeholder(coursePlaceholderDrawable)
                    .centerCrop()
                    .into(coverTarget)

            activeStoryMarker.changeVisibility(!viewedStoryIds.contains(story.id))

            itemView.visibility = if (position == selected) {
                View.INVISIBLE
            } else {
                View.VISIBLE
            }
        }
    }
}