package org.stepic.droid.ui.fragments

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.view_catalog_search_toolbar.*
import kotlinx.android.synthetic.main.view_centered_toolbar.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.core.presenters.SearchCoursesPresenter
import org.stepic.droid.model.CourseListType
import org.stepic.droid.ui.custom.AutoCompleteSearchView
import org.stepic.droid.ui.util.initCenteredToolbar
import javax.inject.Inject

class CourseSearchFragment: CourseListFragmentBase() {
    companion object {
        private const val QUERY_KEY = "query_key"

        fun newInstance(query: String): Fragment {
            val fragment = CourseSearchFragment()
            val bundle = Bundle()
            bundle.putString(QUERY_KEY, query)
            fragment.arguments = bundle
            return fragment
        }

        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }
    }

    private var searchQuery: String? = null

    @Inject
    lateinit var searchCoursesPresenter: SearchCoursesPresenter

    lateinit var searchIcon: ImageView

    override fun injectComponent() {
        App
            .componentManager()
            .courseGeneralComponent()
            .courseListComponentBuilder()
            .build()
            .inject(this)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        searchQuery = arguments?.getString(QUERY_KEY)
        super.onViewCreated(view, savedInstanceState)
        initCenteredToolbar(R.string.search_title, true)
        emptySearch.isClickable = false
        emptySearch.isFocusable = false
        searchCoursesPresenter.attachView(this)
        searchCoursesPresenter.restoreState()
        searchIcon = searchViewToolbar.findViewById(androidx.appcompat.R.id.search_mag_icon) as ImageView
        swipeRefreshLayout.post { searchCoursesPresenter.downloadData(searchQuery) }
        setupSearchBar()
    }

    override fun onDestroyView() {
        searchCoursesPresenter.detachView(this)
        super.onDestroyView()
    }

    override fun getCourseType(): CourseListType? = null

    public override fun showEmptyScreen(isShown: Boolean) {
        if (isShown) {
            emptySearch.visibility = View.VISIBLE
            swipeRefreshLayout.visibility = View.GONE
        } else {
            emptySearch.visibility = View.GONE
            swipeRefreshLayout.visibility = View.VISIBLE
        }
    }

    override fun onNeedDownloadNextPage() {
        searchCoursesPresenter.downloadData(searchQuery)
    }

    override fun onRefresh() {
        searchCoursesPresenter.refreshData(searchQuery)
    }

    private fun setupSearchBar() {
        centeredToolbar.isVisible = false
        backIcon.isVisible = true
        if (android.os.Build.VERSION.SDK_INT < 21) {
            toolbarShadow.isVisible = true
        }
        searchViewToolbar.isVisible = true
        searchViewContainer.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
        searchViewToolbar.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
        searchIcon.setImageResource(0)
        (searchViewToolbar.layoutParams as ViewGroup.MarginLayoutParams).setMargins(0, 0, 0, 0)
        setupSearchView(searchViewToolbar)
        searchViewToolbar.setIconifiedByDefault(false)
        backIcon.setOnClickListener {
            val hasFocus = searchViewToolbar.hasFocus()
            if (hasFocus) {
                searchViewToolbar.clearFocus()
            } else {
                activity?.finish()
            }
        }
    }

    private fun setupSearchView(searchView: AutoCompleteSearchView) {
        searchView.setCloseIconDrawableRes(getCloseIconDrawableRes())
        searchView.setSearchable(requireActivity())
        searchView.initSuggestions(rootView)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                searchView.onSubmitted(query)
                return false
            }

            override fun onQueryTextChange(query: String): Boolean {
                searchView.setConstraint(query)
                return false
            }
        })

        searchView.onActionViewExpanded()
        searchQuery?.let { searchView.setQuery(it, false) }
        searchView.clearFocus()
    }
}