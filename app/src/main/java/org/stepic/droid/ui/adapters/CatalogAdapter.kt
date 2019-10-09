package org.stepic.droid.ui.adapters

import android.support.v7.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Checkable
import kotlinx.android.synthetic.main.catalog_item.view.*
import kotlinx.android.synthetic.main.view_catalog_no_internet_clickable.view.*
import kotlinx.android.synthetic.main.view_catalog_tags.view.*
import kotlinx.android.synthetic.main.view_course_languages.view.*
import kotlinx.android.synthetic.main.view_stories_container.view.*
import org.stepic.droid.R
import org.stepic.droid.configuration.Config
import org.stepic.droid.features.stories.presentation.StoriesView
import org.stepic.droid.features.stories.ui.adapter.StoriesAdapter
import org.stepic.droid.model.*
import org.stepic.droid.ui.custom.CoursesCarouselViewState
import org.stepic.droid.ui.util.changeVisibility
import org.stepic.droid.ui.util.setHeight
import org.stepik.android.model.Tag
import ru.nobird.android.stories.model.Story
import java.util.*

class CatalogAdapter(
    private val config: Config,
    private val courseListItems: List<CoursesCarouselInfo>,
    private val onFiltersChanged: (EnumSet<StepikFilter>) -> Unit,
    private val onRetry: () -> Unit,
    private val onTagClicked: (Tag) -> Unit,
    private val onStoryClicked: (Story, Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val CAROUSEL_TYPE = 0
        private const val LANGUAGES_TYPE = 1
        private const val POPULAR_TYPE = 2
        private const val OFFLINE_TYPE = 3
        private const val TAGS_TYPE = 4
        private const val STORIES_TYPE = 5

        private const val PRE_CAROUSEL_COUNT = 3
        private const val POST_CAROUSEL_COUNT = 1

        const val STORIES_INDEX = 0
        private const val LANGUAGE_INDEX = 1
        private const val TAGS_INDEX = 2
    }

    private var filters: EnumSet<StepikFilter>? = null
    private var needShowFilters = false

    private val coursesCarouselViewStates = SparseArray<CoursesCarouselViewState>()

    private var tags = mutableListOf<Tag>()
    var isOfflineMode: Boolean = false
        private set

    var storiesState: StoriesView.State = StoriesView.State.Idle
        set(value) {
            field = value
            notifyItemChanged(STORIES_INDEX)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            CAROUSEL_TYPE, POPULAR_TYPE -> {
                val view = layoutInflater.inflate(R.layout.catalog_item, parent, false)
                CarouselViewHolder(view)
            }
            LANGUAGES_TYPE -> {
                val view = layoutInflater.inflate(R.layout.view_course_languages, parent, false)
                LanguagesViewHolder(view)
            }
            OFFLINE_TYPE -> {
                val view = layoutInflater.inflate(R.layout.view_catalog_no_internet_clickable, parent, false)
                OfflineViewHolder(view)
            }
            TAGS_TYPE -> {
                val view = layoutInflater.inflate(R.layout.view_catalog_tags, parent, false)
                TagsViewHolder(view, onTagClicked)
            }
            STORIES_TYPE -> {
                val view = layoutInflater.inflate(R.layout.view_stories_container, parent, false)
                StoriesViewHolder(view, config, onStoryClicked)
            }
            else -> throw IllegalStateException("CatalogAdapter viewType = $viewType is unsupported")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            CAROUSEL_TYPE -> {
                holder as CarouselViewHolder
                val coursesCarouselInfo = courseListItemBy(adapterPosition = position)
                val descriptionColors = getDescriptionColors(position)
                holder.bindData(coursesCarouselInfo, descriptionColors, coursesCarouselViewStates[position])
            }
            LANGUAGES_TYPE -> {
                holder as LanguagesViewHolder
                holder.refreshLanguages()
            }
            POPULAR_TYPE -> {
                holder as CarouselViewHolder
                holder.bindData(CoursesCarouselInfoConstants.popular, null, coursesCarouselViewStates[position])
            }
            OFFLINE_TYPE -> {
                holder as OfflineViewHolder
                holder.bindText()
            }
            TAGS_TYPE -> {
                holder as TagsViewHolder
                holder.bindTags(tags)
            }
            STORIES_TYPE -> {
                holder as StoriesViewHolder
                holder.bindState(storiesState)
            }
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        if (holder is CarouselViewHolder) {
            coursesCarouselViewStates.append(holder.adapterPosition, holder.coursesCarousel.onSaveState())
        }
        super.onViewRecycled(holder)
    }

    fun setFilters(filtersFromPreferences: EnumSet<StepikFilter>, needShow: Boolean) {
        filters = filtersFromPreferences
        needShowFilters = needShow
        notifyItemChanged(LANGUAGE_INDEX)
    }

    fun refreshPopular() {
        val popularAdapterIndex = (PRE_CAROUSEL_COUNT + courseListItems.size + POST_CAROUSEL_COUNT - 1)
        notifyItemChanged(popularAdapterIndex)
    }

    private fun getDescriptionColors(position: Int): CollectionDescriptionColors =
            when (position % 2) {
                0 -> CollectionDescriptionColors.FIRE
                1 -> CollectionDescriptionColors.BLUE
                else -> throw IllegalStateException("Use correct divider")
            }

    private fun courseListItemBy(adapterPosition: Int): CoursesCarouselInfo =
            courseListItems[adapterPosition - PRE_CAROUSEL_COUNT]

    override fun getItemCount(): Int {
        var count = PRE_CAROUSEL_COUNT + POST_CAROUSEL_COUNT
        count +=
                if (isOfflineMode) {
                    0 //hide tags & show offline instead
                } else {
                    courseListItems.size
                }

        return count
    }

    override fun getItemViewType(adapterPosition: Int): Int {
        return if (isOfflineMode) {
            getItemViewTypeInOfflineMode(adapterPosition)
        } else {
            getItemViewTypeInOnlineMode(adapterPosition)
        }
    }

    private fun getItemViewTypeInOfflineMode(adapterPosition: Int): Int =
            when (adapterPosition) {
                STORIES_INDEX  -> STORIES_TYPE
                LANGUAGE_INDEX -> LANGUAGES_TYPE
                LANGUAGE_INDEX + 1 -> OFFLINE_TYPE
                LANGUAGE_INDEX + 2 -> POPULAR_TYPE
                else -> throw IllegalStateException("Catalog recycler type is not identified in offline mode")
            }

    private fun getItemViewTypeInOnlineMode(adapterPosition: Int): Int =
            when (adapterPosition) {
                STORIES_INDEX -> STORIES_TYPE
                LANGUAGE_INDEX -> LANGUAGES_TYPE
                TAGS_INDEX -> TAGS_TYPE
                in PRE_CAROUSEL_COUNT until PRE_CAROUSEL_COUNT + courseListItems.size -> CAROUSEL_TYPE
                PRE_CAROUSEL_COUNT + courseListItems.size -> POPULAR_TYPE // after coursesListItems
                else -> throw IllegalStateException("Catalog recycler type is not identified in online mode")
            }


    private class CarouselViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        internal val coursesCarousel = itemView.coursesCarouselItem

        fun bindData(
            coursesCarouselInfo: CoursesCarouselInfo,
            descriptionColors: CollectionDescriptionColors?,
            coursesCarouselViewState: CoursesCarouselViewState?
        ) {
            coursesCarousel.setDescriptionColors(descriptionColors)

            if (coursesCarouselViewState != null) {
                coursesCarousel.onRestoreState(coursesCarouselInfo, coursesCarouselViewState)
            } else {
                coursesCarousel.setCourseCarouselInfo(coursesCarouselInfo)
            }
        }
    }

    private inner class LanguagesViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val languageRu = itemView.languageRu
        private val languageEn = itemView.languageEn

        init {
            val onClickListener = View.OnClickListener { checkableView ->
                checkableView as Checkable
                if (checkableView.isChecked) {
                    //skip click event
                    return@OnClickListener
                }
                languageRu.toggle()
                languageEn.toggle()
                val filters = composeFilters()
                this@CatalogAdapter.filters = filters // apply silently
                onFiltersChanged(filters)
            }

            languageRu.setOnClickListener(onClickListener)
            languageEn.setOnClickListener(onClickListener)
        }

        fun refreshLanguages() {
            val localFilters = filters ?: return
            itemView.setHeight(if (needShowFilters) ViewGroup.LayoutParams.WRAP_CONTENT else 0)
            updateCheckableView(languageRu, localFilters.contains(StepikFilter.RUSSIAN))
            updateCheckableView(languageEn, localFilters.contains(StepikFilter.ENGLISH))
        }

        private fun updateCheckableView(view: Checkable, shouldBeChecked: Boolean) {
            if (view.isChecked != shouldBeChecked) {
                view.isChecked = shouldBeChecked
            }
        }

        private fun composeFilters(): EnumSet<StepikFilter> {
            val filters = EnumSet.noneOf(StepikFilter::class.java)
            if (languageRu.isChecked) {
                filters.add(StepikFilter.RUSSIAN)
            }

            if (languageEn.isChecked) {
                filters.add(StepikFilter.ENGLISH)
            }
            return filters
        }
    }

    private inner class OfflineViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val noInternetView = itemView.noInternetPlaceholder

        init {
            noInternetView.setOnClickListener {
                onRetry()
            }
        }

        fun bindText() {
            noInternetView.setPlaceholderText(R.string.internet_problem_catalog)
        }
    }

    private class TagsViewHolder(view: View,
                                 onTagClicked: (Tag) -> Unit) : RecyclerView.ViewHolder(view) {

        private val tagsRecyclerView = itemView.tagsRecycler
        private val tagsAdapter = TagsAdapter(onTagClicked)

        init {
            tagsRecyclerView.layoutManager = LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)
            tagsRecyclerView.adapter = tagsAdapter
        }

        fun bindTags(tags: List<Tag>) {
            if (tags.isEmpty()) {
                hideTags()
            } else {
                showTags(tags)
            }
        }

        private fun hideTags() {
            itemView.visibility = View.GONE
        }

        private fun showTags(tags: List<Tag>) {
            tagsAdapter.setTags(tags)
            itemView.visibility = View.VISIBLE
        }
    }

    fun enableOfflineMode() {
        isOfflineMode = true
        coursesCarouselViewStates.clear()
        notifyDataSetChanged()
    }

    fun showCollections() {
        isOfflineMode = false
        coursesCarouselViewStates.clear()
        notifyDataSetChanged()
    }

    fun onTagLoaded(tags: List<Tag>) {
        this.tags.clear()
        this.tags.addAll(tags)
        notifyItemChanged(TAGS_INDEX)
    }

    fun onTagNotLoaded() {
        if (this.tags.isEmpty()) {
            notifyItemChanged(TAGS_INDEX)
        }
    }

    class StoriesViewHolder(
        root: View,
        config: Config,
        onStoryClicked: (Story, Int) -> Unit
    ) : RecyclerView.ViewHolder(root) {
        private val loadingPlaceholder = root.storiesContainerLoadingPlaceholder
        val storiesAdapter = StoriesAdapter(root.context, onStoryClicked)
        val recycler = root.storiesRecycler

        init {
            recycler.itemAnimator = null
            recycler.adapter = storiesAdapter
            recycler.layoutManager = LinearLayoutManager(root.context, LinearLayoutManager.HORIZONTAL, false)
        }

        fun bindState(state: StoriesView.State) = when(state) {
            is StoriesView.State.Idle,
            is StoriesView.State.Empty -> {
                itemView.setHeight(0)
                recycler.changeVisibility(false)
                loadingPlaceholder.changeVisibility(false)
            }

            is StoriesView.State.Loading -> {
                itemView.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                recycler.changeVisibility(false)
                loadingPlaceholder.changeVisibility(true)
            }

            is StoriesView.State.Success -> {
                itemView.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                storiesAdapter.setData(state.stories, state.viewedStoriesIds)
                recycler.changeVisibility(true)
                loadingPlaceholder.changeVisibility(false)
            }
        }
    }
}
