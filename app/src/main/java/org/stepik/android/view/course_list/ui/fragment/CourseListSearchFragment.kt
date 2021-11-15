package org.stepik.android.view.course_list.ui.fragment

import android.app.SearchManager
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.get
import kotlinx.android.synthetic.main.empty_search.*
import kotlinx.android.synthetic.main.error_no_connection_with_button.*
import kotlinx.android.synthetic.main.fragment_course_list.*
import kotlinx.android.synthetic.main.view_centered_toolbar.*
import kotlinx.android.synthetic.main.view_search_toolbar.backIcon
import kotlinx.android.synthetic.main.view_search_toolbar.filterIcon
import kotlinx.android.synthetic.main.view_search_toolbar.searchViewToolbar
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.analytic.experiments.InAppPurchaseSplitTest
import org.stepic.droid.base.App
import org.stepic.droid.configuration.RemoteConfig
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.core.presenters.SearchSuggestionsPresenter
import org.stepic.droid.core.presenters.contracts.SearchSuggestionsView
import org.stepic.droid.model.SearchQuery
import org.stepic.droid.model.SearchQuerySource
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.ui.custom.AutoCompleteSearchView
import org.stepic.droid.ui.util.initCenteredToolbar
import org.stepik.android.domain.course.analytic.CourseViewSource
import org.stepik.android.domain.course_payments.mapper.DefaultPromoCodeMapper
import org.stepik.android.domain.filter.model.CourseListFilterQuery
import org.stepik.android.domain.last_step.model.LastStep
import org.stepik.android.domain.search_result.mapper.SearchResultRemoteQueryParamsMapper
import org.stepik.android.domain.search_result.model.SearchResultQuery
import org.stepik.android.model.Course
import org.stepik.android.presentation.course_continue.model.CourseContinueInteractionSource
import org.stepik.android.presentation.course_list.CourseListSearchPresenter
import org.stepik.android.presentation.course_list.CourseListSearchResultView
import org.stepik.android.presentation.course_list.CourseListView
import org.stepik.android.presentation.filter.FilterQueryView
import org.stepik.android.view.course.mapper.DisplayPriceMapper
import org.stepik.android.view.course_list.delegate.CourseContinueViewDelegate
import org.stepik.android.view.course_list.delegate.CourseListViewDelegate
import org.stepik.android.view.filter.ui.dialog.FilterBottomSheetDialogFragment
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.android.core.model.PaginationDirection
import ru.nobird.android.view.base.ui.extension.argument
import ru.nobird.android.view.base.ui.extension.setOnPaginationListener
import ru.nobird.android.view.base.ui.extension.showIfNotExists
import javax.inject.Inject

class CourseListSearchFragment :
    Fragment(R.layout.fragment_course_list),
    CourseListSearchResultView,
    FilterQueryView,
    SearchSuggestionsView,
    AutoCompleteSearchView.FocusCallback,
    AutoCompleteSearchView.SuggestionClickCallback {
    companion object {
        fun newInstance(query: String?, filterQuery: CourseListFilterQuery?): Fragment =
            CourseListSearchFragment().apply {
                this.query = query ?: ""
                this.filterQuery = filterQuery ?: CourseListFilterQuery(language = sharedPreferencesHelper.languageForFeatured)
            }

        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }

        private const val PURCHASE_FLOW_IAP = "iap"
        private const val PURCHASE_FLOW_WEB = "web"
    }

    private var menuDrawableRes: Int = R.drawable.ic_filter
    private lateinit var searchIcon: ImageView

    private var query by argument<String>()
    private var filterQuery by argument<CourseListFilterQuery>()

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

    @Inject
    internal lateinit var searchResultRemoteQueryParamsMapper: SearchResultRemoteQueryParamsMapper

    @Inject
    internal lateinit var defaultPromoCodeMapper: DefaultPromoCodeMapper

    @Inject
    internal lateinit var displayPriceMapper: DisplayPriceMapper

    @Inject
    lateinit var searchSuggestionsPresenter: SearchSuggestionsPresenter

    @Inject
    lateinit var firebaseRemoteConfig: FirebaseRemoteConfig

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

        val searchResultQuery = SearchResultQuery(
            page = 1,
            query = query,
            type = SearchResultQuery.TYPE_COURSE,
            filterQuery = filterQuery,
            remoteQueryParams = searchResultRemoteQueryParamsMapper.buildRemoteQueryParams()
        )
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
            defaultPromoCodeMapper = defaultPromoCodeMapper,
            displayPriceMapper = displayPriceMapper,
            isIAPFlowEnabled = firebaseRemoteConfig[RemoteConfig.PURCHASE_FLOW_ANDROID].asString() == PURCHASE_FLOW_IAP
        )

        courseListPresenter.fetchCourses(searchResultQuery)
    }

    private fun setupSearchBar() {
        centeredToolbar.isVisible = false
        backIcon.isVisible = true
        filterIcon.isVisible = true
        searchViewToolbar.isVisible = true
        searchIcon.setImageResource(0)
        (searchViewToolbar.layoutParams as ViewGroup.MarginLayoutParams).setMargins(0, 0, 0, 0)
        setupSearchView(searchViewToolbar)
        searchViewToolbar.setFocusCallback(this)
        searchViewToolbar.setSuggestionClickCallback(this)
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
                searchSuggestionsPresenter.onQueryTextSubmit(query)
                return false
            }

            override fun onQueryTextChange(query: String): Boolean {
                searchView.setConstraint(query)
                searchSuggestionsPresenter.onQueryTextChange(query)
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

    override fun setState(state: CourseListSearchResultView.State) {
        val courseListState = (state as? CourseListSearchResultView.State.Data)?.courseListViewState ?: CourseListView.State.Idle
        courseListViewDelegate.setState(courseListState)
        (state as? CourseListSearchResultView.State.Data)?.let {
            val defaultFilterQuery = CourseListFilterQuery(language = sharedPreferencesHelper.languageForFeatured)
            menuDrawableRes = if (defaultFilterQuery == it.searchResultQuery.filterQuery) {
                R.drawable.ic_filter
            } else {
                R.drawable.ic_filter_active
            }
            filterIcon.setImageResource(menuDrawableRes)
        }
    }

    override fun showCourse(course: Course, source: CourseViewSource, isAdaptive: Boolean) {
        courseListViewDelegate.showCourse(course, source, isAdaptive)
    }

    override fun showSteps(course: Course, source: CourseViewSource, lastStep: LastStep) {
        courseListViewDelegate.showSteps(course, source, lastStep)
    }

    override fun setBlockingLoading(isLoading: Boolean) {
        courseListViewDelegate.setBlockingLoading(isLoading)
    }

    override fun showNetworkError() {
        courseListViewDelegate.showNetworkError()
    }

    override fun onStart() {
        super.onStart()
        courseListPresenter.attachView(this)
        searchSuggestionsPresenter.attachView(this)
    }

    override fun onStop() {
        searchSuggestionsPresenter.detachView(this)
        courseListPresenter.detachView(this)
        super.onStop()
    }

    override fun showFilterDialog(filterQuery: CourseListFilterQuery) {
        requireActivity().intent.putExtra(SearchManager.QUERY, searchViewToolbar.query.toString())
        FilterBottomSheetDialogFragment
            .newInstance(filterQuery)
            .showIfNotExists(childFragmentManager, FilterBottomSheetDialogFragment.TAG)
    }

    override fun setSuggestions(suggestions: List<SearchQuery>, source: SearchQuerySource) {
        searchViewToolbar.setSuggestions(suggestions, source)
    }

    override fun onFocusChanged(hasFocus: Boolean) {
        if (hasFocus) {
            searchSuggestionsPresenter.onQueryTextChange(searchViewToolbar.query.toString())
        }
    }

    override fun onQueryTextSubmitSuggestion(query: String) {
        searchViewToolbar.setQuery(query, true)
    }
}