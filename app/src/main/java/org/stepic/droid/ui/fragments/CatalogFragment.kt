package org.stepic.droid.ui.fragments

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.view.*
import kotlinx.android.synthetic.main.fragment_catalog.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.base.FragmentBase
import org.stepic.droid.core.presenters.CatalogPresenter
import org.stepic.droid.core.presenters.FiltersPresenter
import org.stepic.droid.core.presenters.contracts.CatalogView
import org.stepic.droid.core.presenters.contracts.FiltersView
import org.stepic.droid.model.CoursesCarouselInfo
import org.stepic.droid.model.StepikFilter
import org.stepic.droid.ui.adapters.CatalogAdapter
import org.stepic.droid.ui.util.SearchHelper
import org.stepic.droid.ui.util.initCenteredToolbar
import java.util.*
import javax.inject.Inject

class CatalogFragment : FragmentBase(),
        CatalogView, FiltersView {

    companion object {
        fun newInstance(): FragmentBase = CatalogFragment()
    }

    @Inject
    lateinit var catalogPresenter: CatalogPresenter

    @Inject
    lateinit var filtersPresenter: FiltersPresenter

    private val courseCarouselInfoList = mutableListOf<CoursesCarouselInfo>()

    private var searchMenuItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun injectComponent() {
        App
                .Companion
                .component()
                .catalogComponentBuilder()
                .build()
                .inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater?.inflate(R.layout.fragment_catalog, container, false)


    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        catalogRecyclerView.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                collapseAndHide()
            }
        }

        initCenteredToolbar(R.string.catalog_title, showHomeButton = false)
        initMainRecycler()

        filtersPresenter.attachView(this)
        catalogPresenter.attachView(this)
        filtersPresenter.onNeedFilters()
        catalogPresenter.onCatalogOpened()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        catalogPresenter.detachView(this)
        filtersPresenter.detachView(this)
    }

    private fun initMainRecycler() {
        catalogRecyclerView.layoutManager = LinearLayoutManager(context)
        catalogRecyclerView.adapter = CatalogAdapter(courseCarouselInfoList, { filtersPresenter.onFilterChanged(it) })
    }

    override fun showCarousels(courseItems: List<CoursesCarouselInfo>) {
        this.courseCarouselInfoList.clear()
        this.courseCarouselInfoList.addAll(courseItems)
        catalogRecyclerView.adapter.notifyDataSetChanged()
    }

    override fun offlineMode() {
        //do nothing
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        searchMenuItem = SearchHelper.createSearch(menu, inflater, activity)
        (searchMenuItem?.actionView as SearchView).setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String?): Boolean = false

            override fun onQueryTextSubmit(query: String?): Boolean {
                collapseAndHide()
                return false
            }

        })
    }


    override fun onDestroyOptionsMenu() {
        super.onDestroyOptionsMenu()

        (searchMenuItem?.actionView as? SearchView)?.setOnQueryTextListener(null)
        searchMenuItem = null
    }

    private fun collapseAndHide() {
        if (searchMenuItem?.isActionViewExpanded == true) {
            hideSoftKeypad()
            searchMenuItem?.collapseActionView()
        }
    }

    override fun onFiltersPrepared(filterForFeatured: EnumSet<StepikFilter>) {
        (catalogRecyclerView.adapter as CatalogAdapter).showFilters(filterForFeatured)
    }

}
