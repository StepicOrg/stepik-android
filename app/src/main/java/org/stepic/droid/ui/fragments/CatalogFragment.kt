package org.stepic.droid.ui.fragments

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.view.*
import kotlinx.android.synthetic.main.fragment_catalog.*
import org.stepic.droid.R
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
import org.stepic.droid.model.CoursesCarouselInfo
import org.stepic.droid.model.StepikFilter
import org.stepic.droid.ui.adapters.CatalogAdapter
import org.stepic.droid.ui.custom.AutoCompleteSearchView
import org.stepic.droid.ui.util.initCenteredToolbar
import org.stepik.android.model.Tag
import java.util.*
import javax.inject.Inject

class CatalogFragment : FragmentBase(),
        CatalogView, FiltersView, FiltersListener, TagsView, StoriesView {

    companion object {
        fun newInstance(): FragmentBase = CatalogFragment()
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

    private val courseCarouselInfoList = mutableListOf<CoursesCarouselInfo>()

    private var searchMenuItem: MenuItem? = null

    private var needShowLangWidget = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun injectComponent() {
        App
                .component()
                .catalogComponentBuilder()
                .build()
                .inject(this)

        needShowLangWidget = sharedPreferenceHelper.isNeedShowLangWidget
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater?.inflate(R.layout.fragment_catalog, container, false)


    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initCenteredToolbar(R.string.catalog_title, showHomeButton = false)
        initMainRecycler()

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
        catalogRecyclerView.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                searchMenuItem?.collapseActionView()
            }
        }
        catalogRecyclerView.itemAnimator = null
        catalogRecyclerView.layoutManager = LinearLayoutManager(context)
        catalogRecyclerView.adapter = CatalogAdapter(courseCarouselInfoList,
                { filtersPresenter.onFilterChanged(it) },
                {
                    filtersPresenter.onNeedFilters()
                    tagsPresenter.onNeedShowTags()
                },
                { tag -> onTagClicked(tag) },
                { _, _ -> }
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)

        searchMenuItem = menu.findItem(R.id.action_search)
        val searchView = searchMenuItem?.actionView as? AutoCompleteSearchView

        searchMenuItem?.setOnMenuItemClickListener {
            analytic.reportEvent(Analytic.Search.SEARCH_OPENED)
            false
        }

        searchView?.let {
            it.initSuggestions(catalogContainer)
            it.setCloseIconDrawableRes(getCloseIconDrawableRes())
            it.setSearchable(activity)

            it.suggestionsOnTouchListener = View.OnTouchListener { _, _ ->
                hideSoftKeypad()
                false
            }

            it.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    it.onSubmitted(query)
                    searchMenuItem?.collapseActionView()
                    return false
                }

                override fun onQueryTextChange(query: String): Boolean {
                    it.setConstraint(query)
                    return false
                }
            })
        }
    }

    override fun onDestroyOptionsMenu() {
        super.onDestroyOptionsMenu()

        (searchMenuItem?.actionView as? SearchView)?.setOnQueryTextListener(null)
        searchMenuItem = null
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
}
