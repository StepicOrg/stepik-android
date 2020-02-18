package org.stepic.droid.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_catalog.*
import kotlinx.android.synthetic.main.view_catalog_search_toolbar.*
import kotlinx.android.synthetic.main.view_centered_toolbar.*
import org.stepic.droid.R
import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.base.Client
import org.stepic.droid.base.FragmentBase
import org.stepic.droid.core.filters.contract.FiltersListener
import org.stepic.droid.core.presenters.CatalogPresenter
import org.stepic.droid.core.presenters.FiltersPresenter
import org.stepic.droid.core.presenters.TagsPresenter
import org.stepic.droid.core.presenters.contracts.CatalogView
import org.stepic.droid.core.presenters.contracts.FiltersView
import org.stepic.droid.core.presenters.contracts.TagsView
import org.stepic.droid.features.stories.presentation.StoriesPresenter
import org.stepic.droid.features.stories.presentation.StoriesView
import org.stepic.droid.features.stories.ui.activity.StoriesActivity
import org.stepic.droid.features.stories.ui.adapter.StoriesAdapter
import org.stepic.droid.model.CoursesCarouselInfo
import org.stepic.droid.model.StepikFilter
import org.stepic.droid.ui.adapters.CatalogAdapter
import org.stepic.droid.ui.custom.AutoCompleteSearchView
import org.stepic.droid.ui.util.initCenteredToolbar
import org.stepik.android.model.Tag
import ru.nobird.android.stories.transition.SharedTransitionIntentBuilder
import ru.nobird.android.stories.transition.SharedTransitionsManager
import ru.nobird.android.stories.ui.delegate.SharedTransitionContainerDelegate
import java.util.EnumSet
import javax.inject.Inject

class CatalogFragment : FragmentBase(),
        CatalogView, FiltersView, FiltersListener, TagsView, StoriesView, AutoCompleteSearchView.FocusCallback {

    companion object {
        fun newInstance(): FragmentBase = CatalogFragment()

        private const val CATALOG_STORIES_KEY = "catalog_stories"

        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }
    }

    @Inject
    lateinit var catalogPresenter: CatalogPresenter

    @Inject
    lateinit var filtersPresenter: FiltersPresenter

    @Inject
    lateinit var filtersClient: Client<FiltersListener>

    @Inject
    lateinit var tagsPresenter: TagsPresenter

    @Inject
    lateinit var storiesPresenter: StoriesPresenter

    lateinit var searchIcon: ImageView

    private val courseCarouselInfoList = mutableListOf<CoursesCarouselInfo>()

    private var needShowLangWidget = false

    // This workaround is necessary, because onFocus get activated multiple times
    private var searchEventLogged: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        analytic.reportAmplitudeEvent(AmplitudeAnalytic.Catalog.CATALOG_SCREEN_OPENED)
        analytic.reportEvent(Analytic.Catalog.CATALOG_SCREEN_OPENED)
    }

    override fun injectComponent() {
        App
            .component()
            .catalogComponentBuilder()
            .build()
            .inject(this)

        needShowLangWidget = sharedPreferenceHelper.isNeedShowLangWidget
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_catalog, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initCenteredToolbar(R.string.catalog_title, showHomeButton = false)
        initMainRecycler()
        searchIcon = searchViewToolbar.findViewById(androidx.appcompat.R.id.search_mag_icon) as ImageView
        setupSearchBar()

        tagsPresenter.attachView(this)
        filtersClient.subscribe(this)
        filtersPresenter.attachView(this)
        catalogPresenter.attachView(this)
        storiesPresenter.attachView(this)
        filtersPresenter.onNeedFilters()
        tagsPresenter.onNeedShowTags()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        tagsPresenter.detachView(this)
        filtersClient.unsubscribe(this)
        catalogPresenter.detachView(this)
        storiesPresenter.detachView(this)
        filtersPresenter.detachView(this)
    }

    private fun initMainRecycler() {
        catalogRecyclerView.itemAnimator = null
        catalogRecyclerView.layoutManager = LinearLayoutManager(context)
        catalogRecyclerView.adapter = CatalogAdapter(
            config = config,
            courseListItems = courseCarouselInfoList,
            onFiltersChanged = { filtersPresenter.onFilterChanged(it) },
            onRetry = {
                filtersPresenter.onNeedFilters()
                tagsPresenter.onNeedShowTags()
            },
            onTagClicked = { tag -> onTagClicked(tag) },
            onStoryClicked = { _, position -> showStories(position) }
        )
    }

    private fun onTagClicked(tag: Tag) {
        screenManager.showListOfTag(activity, tag)
    }

    override fun showCollections(courseItems: List<CoursesCarouselInfo>) {
        this.courseCarouselInfoList.clear()
        this.courseCarouselInfoList.addAll(courseItems)
        val catalogAdapter = catalogRecyclerView.adapter as CatalogAdapter
        catalogAdapter.showCollections()
    }

    override fun offlineMode() {
        val catalogAdapter = catalogRecyclerView.adapter as CatalogAdapter
        catalogAdapter.enableOfflineMode()
    }

    private fun setupSearchBar() {
        centeredToolbar.isVisible = false
        if (android.os.Build.VERSION.SDK_INT < 21) {
            toolbarShadow.isVisible = true
        }
        searchViewToolbar.isVisible = true
        searchViewToolbar.onActionViewExpanded()
        searchViewToolbar.clearFocus()
        searchViewToolbar.setIconifiedByDefault(false)
        setupSearchView(searchViewToolbar)
        searchViewToolbar.setFocusCallback(this)
        backIcon.setOnClickListener {
            searchViewToolbar.onActionViewCollapsed()
            searchViewToolbar.onActionViewExpanded()
            searchViewToolbar.clearFocus()
        }
    }

    override fun onFocusChanged(hasFocus: Boolean) {
        backIcon.isVisible = hasFocus
        if (hasFocus) {
            if (!searchEventLogged) {
                logSearchEvent()
                searchEventLogged = true
            }
            searchIcon.setImageResource(0)
            (searchViewToolbar.layoutParams as ViewGroup.MarginLayoutParams).setMargins(0, 0, 0, 0)
            searchViewContainer.setBackgroundResource(R.color.white)
            searchViewToolbar.setBackgroundResource(R.color.white)
        } else {
            searchIcon.setImageResource(R.drawable.ic_action_search)
            val margin = resources.getDimension(R.dimen.search_bar_margin).toInt()
            (searchViewToolbar.layoutParams as ViewGroup.MarginLayoutParams).setMargins(margin, margin, margin, margin)
            searchViewContainer.setBackgroundResource(R.color.new_primary_color)
            searchViewToolbar.setBackgroundResource(R.drawable.bg_catalog_search_bar)
        }
    }

    private fun setupSearchView(searchView: AutoCompleteSearchView?) {
        searchView?.let {
            it.initSuggestions(catalogContainer)
            it.setCloseIconDrawableRes(getCloseIconDrawableRes())
            it.setSearchable(requireActivity())

            it.suggestionsOnTouchListener = View.OnTouchListener { _, _ ->
                hideSoftKeypad()
                false
            }

            it.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    it.onSubmitted(query)
                    searchView.onActionViewCollapsed()
                    searchView.onActionViewExpanded()
                    searchView.clearFocus()
                    return false
                }

                override fun onQueryTextChange(query: String): Boolean {
                    it.setConstraint(query)
                    return false
                }
            })
        }
    }

    override fun onFiltersPrepared(filters: EnumSet<StepikFilter>) {
        updateFilters(filters)
    }

    override fun onFiltersChanged(filters: EnumSet<StepikFilter>) {
        updateFilters(filters)
    }

    private fun updateFilters(filters: EnumSet<StepikFilter>) {
        val catalogAdapter = catalogRecyclerView.adapter as CatalogAdapter
        catalogAdapter.setFilters(filters, needShowLangWidget)
        catalogAdapter.refreshPopular()

        catalogPresenter.onNeedLoadCatalog(filters)
    }

    override fun onTagsFetched(tags: List<Tag>) {
        val catalogAdapter = catalogRecyclerView.adapter as CatalogAdapter
        catalogAdapter.onTagLoaded(tags)
    }

    override fun onTagsNotLoaded() {
        val catalogAdapter = catalogRecyclerView.adapter as CatalogAdapter
        catalogAdapter.onTagNotLoaded()
    }

    override fun setState(state: StoriesView.State) {
        val catalogAdapter = catalogRecyclerView.adapter as CatalogAdapter
        catalogAdapter.storiesState = state
    }

    private fun showStories(position: Int) {
        val storiesViewHolder = catalogRecyclerView.findViewHolderForAdapterPosition(CatalogAdapter.STORIES_INDEX)
                as? CatalogAdapter.StoriesViewHolder
                ?: return

        val stories = storiesViewHolder.storiesAdapter.stories

        requireContext().startActivity(SharedTransitionIntentBuilder.createIntent(
                requireContext(), StoriesActivity::class.java, CATALOG_STORIES_KEY, position, stories
        ))
    }

    override fun onStart() {
        super.onStart()
        SharedTransitionsManager.registerTransitionDelegate(CATALOG_STORIES_KEY, object : SharedTransitionContainerDelegate {
            override fun getSharedView(position: Int): View? {
                val storiesViewHolder = catalogRecyclerView.findViewHolderForAdapterPosition(CatalogAdapter.STORIES_INDEX)
                        as? CatalogAdapter.StoriesViewHolder
                        ?: return null

                val storyViewHolder = storiesViewHolder.recycler.findViewHolderForAdapterPosition(position)
                        as? StoriesAdapter.StoryViewHolder
                        ?: return null

                return storyViewHolder.cover
            }

            override fun onPositionChanged(position: Int) {
                val storiesViewHolder = catalogRecyclerView.findViewHolderForAdapterPosition(CatalogAdapter.STORIES_INDEX)
                        as? CatalogAdapter.StoriesViewHolder
                        ?: return

                storiesViewHolder.recycler.layoutManager?.scrollToPosition(position)
                storiesViewHolder.storiesAdapter.selected = position

                if (position != -1) {
                    val story = storiesViewHolder.storiesAdapter.stories[position]
                    storiesPresenter.onStoryViewed(story.id)
                    analytic.reportAmplitudeEvent(AmplitudeAnalytic.Stories.STORY_OPENED, mapOf(
                            AmplitudeAnalytic.Stories.Values.STORY_ID to story.id
                    ))
                }
            }
        })
    }

    override fun onStop() {
        SharedTransitionsManager.unregisterTransitionDelegate(CATALOG_STORIES_KEY)
        super.onStop()
    }

    private fun logSearchEvent() {
        analytic.reportEvent(Analytic.Search.SEARCH_OPENED)
        analytic.reportAmplitudeEvent(AmplitudeAnalytic.Search.COURSE_SEARCH_CLICKED)
    }
}
