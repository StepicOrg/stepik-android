package org.stepik.android.view.catalog_block.ui.fragment

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_catalog.*
import kotlinx.android.synthetic.main.view_catalog_search_toolbar.*
import kotlinx.android.synthetic.main.view_centered_toolbar.*
import org.stepic.droid.R
import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.analytic.experiments.InAppPurchaseSplitTest
import org.stepic.droid.base.App
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.features.stories.ui.activity.StoriesActivity
import org.stepic.droid.features.stories.ui.adapter.StoriesAdapter
import org.stepic.droid.ui.custom.AutoCompleteSearchView
import org.stepic.droid.ui.util.CloseIconHolder
import org.stepic.droid.ui.util.initCenteredToolbar
import org.stepik.android.presentation.catalog_block.CatalogFeature
import org.stepik.android.presentation.catalog_block.CatalogViewModel
import org.stepik.android.presentation.course_list_redux.CourseListFeature
import org.stepik.android.presentation.filter.FiltersFeature
import org.stepik.android.presentation.stories.StoriesFeature
import org.stepik.android.view.catalog.ui.adapter.delegate.OfflineAdapterDelegate
import org.stepik.android.view.catalog.ui.adapter.delegate.LoadingAdapterDelegate
import org.stepik.android.view.catalog.ui.adapter.delegate.FiltersAdapterDelegate
import org.stepik.android.view.catalog.ui.adapter.delegate.StoriesAdapterDelegate
import org.stepik.android.view.catalog_block.model.CatalogItem
import org.stepik.android.view.catalog_block.ui.adapter.delegate.CourseListAdapterDelegate
import ru.nobird.android.presentation.redux.container.ReduxView
import ru.nobird.android.stories.transition.SharedTransitionIntentBuilder
import ru.nobird.android.stories.transition.SharedTransitionsManager
import ru.nobird.android.stories.ui.delegate.SharedTransitionContainerDelegate
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter
import ru.nobird.android.view.base.ui.extension.hideKeyboard
import timber.log.Timber
import javax.inject.Inject

class CatalogBlockFragment : Fragment(R.layout.fragment_catalog), ReduxView<CatalogFeature.State, CatalogFeature.Action.ViewAction>, AutoCompleteSearchView.FocusCallback {
    companion object {
        const val TAG = "CatalogBlockFragment"

        fun newInstance(): Fragment =
            CatalogBlockFragment()

        private const val CATALOG_STORIES_INDEX = 0
        private const val CATALOG_STORIES_KEY = "catalog_stories"
    }

    @Inject
    internal lateinit var screenManager: ScreenManager

    @Inject
    internal lateinit var analytic: Analytic

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    internal lateinit var inAppPurchaseSplitTest: InAppPurchaseSplitTest

    private lateinit var searchIcon: ImageView

    // This workaround is necessary, because onFocus get activated multiple times
    private var searchEventLogged: Boolean = false

    private val catalogViewModel: CatalogViewModel by viewModels { viewModelFactory } // by reduxViewModels

    private var catalogItemAdapter: DefaultDelegateAdapter<CatalogItem> = DefaultDelegateAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectComponent()
        analytic.reportAmplitudeEvent(AmplitudeAnalytic.Catalog.CATALOG_SCREEN_OPENED)
        catalogViewModel.onNewMessage(CatalogFeature.Message.StoriesMessage(StoriesFeature.Message.InitMessage()))
        catalogViewModel.onNewMessage(CatalogFeature.Message.FiltersMessage(FiltersFeature.Message.InitMessage()))
        catalogViewModel.onNewMessage(CatalogFeature.Message.InitMessage())
        Timber.d("onCreate")
    }

    private fun injectComponent() {
        App.component()
            .catalogBlockComponentBuilder()
            .build()
            .inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initCenteredToolbar(R.string.catalog_title, showHomeButton = false)
        searchIcon = searchViewToolbar.findViewById(androidx.appcompat.R.id.search_mag_icon) as ImageView
        setupSearchBar()

        catalogItemAdapter += StoriesAdapterDelegate(
            onStoryClicked = { _, position -> showStories(position) }
        )

        catalogItemAdapter += FiltersAdapterDelegate(
            onFiltersChanged = {
                if (catalogRecyclerView.isComputingLayout) return@FiltersAdapterDelegate
                catalogViewModel.onNewMessage(CatalogFeature.Message.FiltersMessage(FiltersFeature.Message.FiltersChanged(it)))
            }
        )

        catalogItemAdapter += OfflineAdapterDelegate {
            catalogViewModel.onNewMessage(CatalogFeature.Message.InitMessage(forceUpdate = true))
        }
        catalogItemAdapter += LoadingAdapterDelegate()
        catalogItemAdapter += CourseListAdapterDelegate(
            analytic = analytic,
            isHandleInAppPurchase = inAppPurchaseSplitTest.currentGroup.isInAppPurchaseActive,
            sendLoadingMessage = { id, fullCourseList ->
                catalogViewModel.onNewMessage(CatalogFeature.Message.CourseListMessage(id = id, message = CourseListFeature.Message.InitMessage(id = id, fullCourseList = fullCourseList)))
            }
        )

        with(catalogRecyclerView) {
            adapter = catalogItemAdapter
            layoutManager = LinearLayoutManager(context)
            itemAnimator = null
        }
    }

    override fun onStart() {
        super.onStart()
        catalogViewModel.attachView(this)
        catalogItemAdapter.notifyDataSetChanged()
        SharedTransitionsManager.registerTransitionDelegate(CATALOG_STORIES_KEY, object :
            SharedTransitionContainerDelegate {
            override fun getSharedView(position: Int): View? {
                val storiesViewHolder = catalogRecyclerView.findViewHolderForAdapterPosition(
                    CATALOG_STORIES_INDEX
                )
                        as? StoriesAdapterDelegate.StoriesViewHolder
                    ?: return null

                val storyViewHolder = storiesViewHolder.storiesRecycler.findViewHolderForAdapterPosition(position)
                        as? StoriesAdapter.StoryViewHolder
                    ?: return null

                return storyViewHolder.cover
            }

            override fun onPositionChanged(position: Int) {
                val storiesViewHolder = catalogRecyclerView.findViewHolderForAdapterPosition(
                    CATALOG_STORIES_INDEX
                )
                        as? StoriesAdapterDelegate.StoriesViewHolder
                    ?: return

                storiesViewHolder.storiesRecycler.layoutManager?.scrollToPosition(position)
                storiesViewHolder.storiesAdapter.selected = position

                if (position != -1) {
                    val story = storiesViewHolder.storiesAdapter.stories[position]
                    catalogViewModel.onNewMessage(CatalogFeature.Message.StoriesMessage(StoriesFeature.Message.StoryViewed(story.id)))
                    analytic.reportAmplitudeEvent(AmplitudeAnalytic.Stories.STORY_OPENED, mapOf(
                        AmplitudeAnalytic.Stories.Values.STORY_ID to story.id
                    ))
                }
            }
        })
    }

    override fun onStop() {
        catalogViewModel.detachView(this)
        SharedTransitionsManager.unregisterTransitionDelegate(CATALOG_STORIES_KEY)
        super.onStop()
    }

    override fun onAction(action: CatalogFeature.Action.ViewAction) {}

    override fun render(state: CatalogFeature.State) {
        val collectionCatalogItems = when (state.collectionsState) {
            is CatalogFeature.CollectionsState.Error ->
                listOf(CatalogItem.Offline)

            is CatalogFeature.CollectionsState.Loading ->
                listOf(CatalogItem.Loading)

            is CatalogFeature.CollectionsState.Content ->
                state.collectionsState.collections.map { CatalogItem.Block(it) }

            else ->
                listOf()
        }

        catalogRecyclerView.post {
            catalogItemAdapter.items = listOf(
                CatalogItem.Stories(state = state.storiesState),
                CatalogItem.Filters(state = state.filtersState)
            ) + collectionCatalogItems
        }

        Timber.d("State: ${state.collectionsState}")
    }

    private fun showStories(position: Int) {
        val storiesViewHolder = catalogRecyclerView.findViewHolderForAdapterPosition(CATALOG_STORIES_INDEX)
                as? StoriesAdapterDelegate.StoriesViewHolder
            ?: return

        val stories = storiesViewHolder.storiesAdapter.stories

        requireContext().startActivity(
            SharedTransitionIntentBuilder.createIntent(
            requireContext(), StoriesActivity::class.java,
                CATALOG_STORIES_KEY, position, stories)
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
            searchViewToolbar.setBackgroundColor(0)
            (searchViewToolbar.layoutParams as ViewGroup.MarginLayoutParams).setMargins(0, 0, 0, 0)
        } else {
            searchIcon.setImageResource(R.drawable.ic_action_search)
            val margin = resources.getDimensionPixelOffset(R.dimen.search_bar_margin)
            searchViewToolbar.setBackgroundResource(R.drawable.bg_catalog_search_bar)
            (searchViewToolbar.layoutParams as ViewGroup.MarginLayoutParams).setMargins(margin, margin, margin, margin)
        }
    }

    private fun setupSearchBar() {
        centeredToolbar.isVisible = false
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
            it.setCloseIconDrawableRes(CloseIconHolder.getCloseIconDrawableRes())
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

    private fun logSearchEvent() {
        analytic.reportEvent(Analytic.Search.SEARCH_OPENED)
        analytic.reportAmplitudeEvent(AmplitudeAnalytic.Search.COURSE_SEARCH_CLICKED)
    }
}