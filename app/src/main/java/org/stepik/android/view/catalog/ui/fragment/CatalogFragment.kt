package org.stepik.android.view.catalog.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_catalog.*
import kotlinx.android.synthetic.main.view_catalog_search_toolbar.*
import kotlinx.android.synthetic.main.view_centered_toolbar.*
import org.stepic.droid.R
import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.features.stories.presentation.StoriesPresenter
import org.stepic.droid.features.stories.ui.activity.StoriesActivity
import org.stepic.droid.features.stories.ui.adapter.StoriesAdapter
import org.stepic.droid.ui.custom.AutoCompleteSearchView
import org.stepic.droid.ui.util.CloseIconHolder.getCloseIconDrawableRes
import org.stepic.droid.ui.util.initCenteredToolbar
import org.stepik.android.presentation.catalog.CatalogPresenter
import org.stepik.android.presentation.catalog.CatalogView
import org.stepik.android.presentation.catalog.model.CatalogItem
import org.stepik.android.presentation.catalog.model.LoadingPlaceholder
import org.stepik.android.presentation.catalog.model.OfflinePlaceholder
import org.stepik.android.view.catalog.ui.adapter.delegate.CourseListAdapterDelegate
import org.stepik.android.view.catalog.ui.adapter.delegate.CourseListQueryAdapterDelegate
import org.stepik.android.view.catalog.ui.adapter.delegate.FiltersAdapterDelegate
import org.stepik.android.view.catalog.ui.adapter.delegate.LoadingAdapterDelegate
import org.stepik.android.view.catalog.ui.adapter.delegate.OfflineAdapterDelegate
import org.stepik.android.view.catalog.ui.adapter.delegate.StoriesAdapterDelegate
import org.stepik.android.view.catalog.ui.adapter.delegate.TagsAdapterDelegate
import org.stepik.android.view.course_list.delegate.CourseContinueViewDelegate
import ru.nobird.android.stories.transition.SharedTransitionIntentBuilder
import ru.nobird.android.stories.transition.SharedTransitionsManager
import ru.nobird.android.stories.ui.delegate.SharedTransitionContainerDelegate
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter
import ru.nobird.android.view.base.ui.extension.hideKeyboard
import javax.inject.Inject

class CatalogFragment : Fragment(), CatalogView, AutoCompleteSearchView.FocusCallback {
    companion object {
        fun newInstance(): Fragment =
            CatalogFragment()

        private const val CATALOG_STORIES_INDEX = 0
        private const val CATALOG_STORIES_KEY = "catalog_stories"
    }

    @Inject
    internal lateinit var screenManager: ScreenManager

    @Inject
    internal lateinit var analytic: Analytic

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var searchIcon: ImageView

    private lateinit var catalogPresenter: CatalogPresenter

    private var catalogItemAdapter: DefaultDelegateAdapter<CatalogItem> = DefaultDelegateAdapter()

    // This workaround is necessary, because onFocus get activated multiple times
    private var searchEventLogged: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectComponent()
        // TODO analytic.reportAmplitudeEvent(AmplitudeAnalytic.Catalog.CATALOG_SCREEN_OPENED)

        catalogPresenter = ViewModelProviders
            .of(this, viewModelFactory)
            .get(CatalogPresenter::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_catalog, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initCenteredToolbar(R.string.catalog_title, showHomeButton = false)
        searchIcon = searchViewToolbar.findViewById(androidx.appcompat.R.id.search_mag_icon) as ImageView
        setupSearchBar()

        catalogItemAdapter += StoriesAdapterDelegate(
            onStoryClicked = { _, position -> showStories(position) }
        )

        catalogItemAdapter += TagsAdapterDelegate { tag -> screenManager.showCoursesByTag(requireContext(), tag) }

        catalogItemAdapter += FiltersAdapterDelegate()

        val courseContinueViewDelegate = CourseContinueViewDelegate(
            activity = requireActivity(),
            analytic = analytic,
            screenManager = screenManager
        )

        catalogItemAdapter += CourseListAdapterDelegate(
            analytic = analytic,
            screenManager = screenManager,
            courseContinueViewDelegate = courseContinueViewDelegate
        )

        catalogItemAdapter += CourseListQueryAdapterDelegate(
            screenManager = screenManager,
            courseContinueViewDelegate = courseContinueViewDelegate
        )

        catalogItemAdapter += OfflineAdapterDelegate { catalogPresenter.fetchCollections(forceUpdate = true) }

        catalogItemAdapter += LoadingAdapterDelegate()

        with(catalogRecyclerView) {
            adapter = catalogItemAdapter
            layoutManager = LinearLayoutManager(context)
            itemAnimator = null
        }
        catalogPresenter.fetchCollections()
    }

    override fun setState(state: CatalogView.State) {
        catalogItemAdapter.items =
            when (val collectionsState = state.collectionsState) {
                is CatalogView.CollectionsState.Loading ->
                    state.headers + listOf(LoadingPlaceholder) + state.footers

                is CatalogView.CollectionsState.Content ->
                    state.headers + collectionsState.collections + state.footers

                is CatalogView.CollectionsState.Error ->
                    state.headers + listOf(OfflinePlaceholder) + state.footers

                else ->
                    state.headers
        }
    }

    private fun showStories(position: Int) {
        val storiesViewHolder = catalogRecyclerView.findViewHolderForAdapterPosition(CATALOG_STORIES_INDEX)
            as? StoriesAdapterDelegate.StoriesViewHolder
            ?: return

        val stories = storiesViewHolder.storiesAdapter.stories

        requireContext().startActivity(SharedTransitionIntentBuilder.createIntent(
            requireContext(), StoriesActivity::class.java, CATALOG_STORIES_KEY, position, stories)
        )
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
            searchViewContainer.setBackgroundResource(R.color.old_cover)
            searchViewToolbar.setBackgroundResource(R.drawable.bg_catalog_search_bar)
        }
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

    private fun setupSearchView(searchView: AutoCompleteSearchView?) {
        searchView?.let {
            it.initSuggestions(catalogContainer)
            it.setCloseIconDrawableRes(getCloseIconDrawableRes())
            it.setSearchable(requireActivity())

            it.suggestionsOnTouchListener = View.OnTouchListener { _, _ ->
                it.hideKeyboard()
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

    private fun injectComponent() {
        App.component()
            .catalogNewComponentBuilder()
            .build()
            .inject(this)
    }

    override fun onStart() {
        super.onStart()
        catalogItemAdapter.notifyDataSetChanged() // re-attach existing view holders
        catalogPresenter.attachView(this)
        SharedTransitionsManager.registerTransitionDelegate(CATALOG_STORIES_KEY, object : SharedTransitionContainerDelegate {
            override fun getSharedView(position: Int): View? {
                val storiesViewHolder = catalogRecyclerView.findViewHolderForAdapterPosition(CATALOG_STORIES_INDEX)
                        as? StoriesAdapterDelegate.StoriesViewHolder
                    ?: return null

                val storyViewHolder = storiesViewHolder.storiesRecycler.findViewHolderForAdapterPosition(position)
                        as? StoriesAdapter.StoryViewHolder
                    ?: return null

                return storyViewHolder.cover
            }

            override fun onPositionChanged(position: Int) {
                val storiesViewHolder = catalogRecyclerView.findViewHolderForAdapterPosition(CATALOG_STORIES_INDEX)
                        as? StoriesAdapterDelegate.StoriesViewHolder
                    ?: return

                storiesViewHolder.storiesRecycler.layoutManager?.scrollToPosition(position)
                storiesViewHolder.storiesAdapter.selected = position

                if (position != -1) {
                    val story = storiesViewHolder.storiesAdapter.stories[position]
                    (catalogItemAdapter.items[CATALOG_STORIES_INDEX] as StoriesPresenter).onStoryViewed(story.id)
                    analytic.reportAmplitudeEvent(AmplitudeAnalytic.Stories.STORY_OPENED, mapOf(
                        AmplitudeAnalytic.Stories.Values.STORY_ID to story.id
                    ))
                }
            }
        })
    }

    override fun onStop() {
        SharedTransitionsManager.unregisterTransitionDelegate(CATALOG_STORIES_KEY)
        catalogPresenter.detachView(this)
        super.onStop()
    }

    private fun logSearchEvent() {
        analytic.reportEvent(Analytic.Search.SEARCH_OPENED)
        analytic.reportAmplitudeEvent(AmplitudeAnalytic.Search.COURSE_SEARCH_CLICKED)
    }
}