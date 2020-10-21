package org.stepik.android.view.course_list.ui.fragment

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.empty_search.*
import kotlinx.android.synthetic.main.error_no_connection_with_button.*
import kotlinx.android.synthetic.main.fragment_course_list.*
import kotlinx.android.synthetic.main.view_catalog_search_toolbar.*
import kotlinx.android.synthetic.main.view_centered_toolbar.*
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.analytic.experiments.InAppPurchaseSplitTest
import org.stepic.droid.base.App
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.ui.custom.AutoCompleteSearchView
import org.stepic.droid.ui.util.initCenteredToolbar
import org.stepic.droid.ui.util.setOnPaginationListener
import org.stepic.droid.util.resolveResourceIdAttribute
import org.stepik.android.domain.base.PaginationDirection
import org.stepik.android.domain.course.analytic.CourseViewSource
import org.stepik.android.domain.filter.model.CourseListFilterQuery
import org.stepik.android.domain.search_result.model.SearchResultQuery
import org.stepik.android.presentation.course_continue.model.CourseContinueInteractionSource
import org.stepik.android.presentation.course_list.CourseListSearchPresenter
import org.stepik.android.presentation.course_list.CourseListView
import org.stepik.android.view.course_list.delegate.CourseContinueViewDelegate
import org.stepik.android.view.course_list.delegate.CourseListViewDelegate
import org.stepik.android.view.filter.ui.dialog.FilterBottomSheetDialogFragment
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.android.view.base.ui.extension.argument
import ru.nobird.android.view.base.ui.extension.showIfNotExists
import javax.inject.Inject

class CourseListSearchFragment : Fragment(R.layout.fragment_course_list), FilterBottomSheetDialogFragment.Callback {
    companion object {
        fun newInstance(query: String?): Fragment =
            CourseListSearchFragment().apply {
                this.query = query ?: ""
            }

        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }
    }

    private lateinit var searchIcon: ImageView

    private var query by argument<String>()

    @Inject
    internal lateinit var analytic: Analytic

    @Inject
    internal lateinit var screenManager: ScreenManager

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    internal lateinit var sharedPreferencesHelper: SharedPreferenceHelper

    @Inject
    internal lateinit var inAppPurchaseSplitTest: InAppPurchaseSplitTest

    private lateinit var courseListViewDelegate: CourseListViewDelegate
    private val courseListPresenter: CourseListSearchPresenter by viewModels { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectComponent()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initCenteredToolbar(query, true)
        searchIcon = searchViewToolbar.findViewById(androidx.appcompat.R.id.search_mag_icon) as ImageView
        setupSearchBar()

        with(courseListCoursesRecycler) {
            layoutManager = GridLayoutManager(context, resources.getInteger(R.integer.course_list_columns))
            setOnPaginationListener { pageDirection ->
                if (pageDirection == PaginationDirection.NEXT) {
                    courseListPresenter.fetchNextPage()
                }
            }
        }

        goToCatalog.setOnClickListener { screenManager.showCatalog(requireContext()) }

        val searchResultQuery = SearchResultQuery(page = 1, query = query, filterQuery = CourseListFilterQuery(language = sharedPreferencesHelper.languageForFeatured))
        courseListSwipeRefresh.setOnRefreshListener {
            courseListPresenter.fetchCourses(
                searchResultQuery,
                forceUpdate = true
            )
        }
        tryAgain.setOnClickListener {
            courseListPresenter.fetchCourses(
                searchResultQuery,
                forceUpdate = true
            )
        }

        val viewStateDelegate = ViewStateDelegate<CourseListView.State>()
        viewStateDelegate.addState<CourseListView.State.Idle>()
        viewStateDelegate.addState<CourseListView.State.Loading>(courseListCoursesRecycler)
        viewStateDelegate.addState<CourseListView.State.Content>(courseListCoursesRecycler)
        viewStateDelegate.addState<CourseListView.State.Empty>(courseListCoursesEmpty)
        viewStateDelegate.addState<CourseListView.State.NetworkError>(courseListCoursesLoadingErrorVertical)

        courseListViewDelegate = CourseListViewDelegate(
            analytic = analytic,
            courseContinueViewDelegate = CourseContinueViewDelegate(
                activity = requireActivity(),
                analytic = analytic,
                screenManager = screenManager
            ),
            courseListSwipeRefresh = courseListSwipeRefresh,
            courseItemsRecyclerView = courseListCoursesRecycler,
            courseListViewStateDelegate = viewStateDelegate,
            onContinueCourseClicked = { courseListItem ->
                courseListPresenter
                    .continueCourse(
                        course = courseListItem.course,
                        viewSource = CourseViewSource.Search(searchResultQuery),
                        interactionSource = CourseContinueInteractionSource.COURSE_WIDGET
                    )
            },
            isHandleInAppPurchase = inAppPurchaseSplitTest.currentGroup.isInAppPurchaseActive,
            onFilterClicked = {
                FilterBottomSheetDialogFragment
                    .newInstance(filterQuery = it)
                    .showIfNotExists(childFragmentManager, FilterBottomSheetDialogFragment.TAG)
            }
        )

        courseListPresenter.fetchCourses(searchResultQuery)
    }

    private fun setupSearchBar() {
        ImageViewCompat.setImageTintList(filterIcon, AppCompatResources.getColorStateList(requireContext(), requireContext().resolveResourceIdAttribute(R.attr.colorControlNormal)))
        centeredToolbar.isVisible = false
        backIcon.isVisible = true
        filterIcon.isVisible = true
        searchViewToolbar.isVisible = true
        searchIcon.setImageResource(0)
        (searchViewToolbar.layoutParams as ViewGroup.MarginLayoutParams).setMargins(0, 0, 0, 0)
        setupSearchView(searchViewToolbar)
        searchViewToolbar.setIconifiedByDefault(false)
        searchViewToolbar.setBackgroundColor(0)
        backIcon.setOnClickListener {
            val hasFocus = searchViewToolbar.hasFocus()
            if (hasFocus) {
                searchViewToolbar.clearFocus()
            } else {
                activity?.finish()
            }
        }
        filterIcon.setOnClickListener {
            courseListPresenter.onFilterMenuItemClicked()
        }
    }

    private fun setupSearchView(searchView: AutoCompleteSearchView) {
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
        query.let { searchView.setQuery(it, false) }
        searchView.clearFocus()
    }

    private fun injectComponent() {
        App.component()
            .courseListComponentBuilder()
            .build()
            .inject(this)
    }

    override fun onStart() {
        super.onStart()
        courseListPresenter.attachView(courseListViewDelegate)
    }

    override fun onStop() {
        courseListPresenter.detachView(courseListViewDelegate)
        super.onStop()
    }

    override fun onSyncFilterQueryWithParent(filterQuery: CourseListFilterQuery) {
        courseListPresenter.fetchCourses(
            searchResultQuery = SearchResultQuery(page = 1, query = query, filterQuery = filterQuery),
            forceUpdate = true
        )
    }
}