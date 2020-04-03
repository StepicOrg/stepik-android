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
import kotlinx.android.synthetic.main.fragment_catalog.*
import kotlinx.android.synthetic.main.view_catalog_search_toolbar.*
import kotlinx.android.synthetic.main.view_centered_toolbar.*
import org.stepic.droid.R
import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.ui.custom.AutoCompleteSearchView
import org.stepic.droid.ui.util.CloseIconHolder.getCloseIconDrawableRes
import org.stepic.droid.ui.util.initCenteredToolbar
import org.stepik.android.presentation.catalog.CatalogPresenter
import org.stepik.android.presentation.catalog.CatalogView
import ru.nobird.android.view.base.ui.extension.hideKeyboard
import javax.inject.Inject

class CatalogFragment : Fragment(), CatalogView, AutoCompleteSearchView.FocusCallback {
    companion object {
        fun newInstance(): Fragment =
            CatalogFragment()
    }

    @Inject
    internal lateinit var analytic: Analytic

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var searchIcon: ImageView

    private lateinit var catalogPresenter: CatalogPresenter

    // This workaround is necessary, because onFocus get activated multiple times
    private var searchEventLogged: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectComponent()

        catalogPresenter = ViewModelProviders
            .of(this, viewModelFactory)
            .get(CatalogPresenter::class.java)

        // TODO Initialize AdapterDelegates
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_catalog_new, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initCenteredToolbar(R.string.catalog_title, showHomeButton = false)
        searchIcon = searchViewToolbar.findViewById(androidx.appcompat.R.id.search_mag_icon) as ImageView
        setupSearchBar()
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
        catalogPresenter.attachView(this)
    }

    override fun onStop() {
        catalogPresenter.detachView(this)
        super.onStop()
    }

    private fun logSearchEvent() {
        analytic.reportEvent(Analytic.Search.SEARCH_OPENED)
        analytic.reportAmplitudeEvent(AmplitudeAnalytic.Search.COURSE_SEARCH_CLICKED)
    }
}