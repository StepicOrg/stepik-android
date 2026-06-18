package org.stepik.android.view.course_list.ui.fragment

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import dev.androidbroadcast.vbpd.viewBinding
import org.stepic.droid.R
import org.stepic.droid.databinding.FragmentCourseListBinding
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.ui.util.initCenteredToolbar
import org.stepik.android.domain.course.analytic.CourseViewSource
import org.stepik.android.domain.course_list.model.CourseListQuery
import org.stepik.android.domain.course_payments.mapper.DefaultPromoCodeMapper
import org.stepik.android.domain.filter.model.CourseListFilterQuery
import org.stepik.android.domain.last_step.model.LastStep
import org.stepik.android.model.Course
import org.stepik.android.presentation.course_continue.model.CourseContinueInteractionSource
import org.stepik.android.presentation.course_list.CourseListQueryPresenter
import org.stepik.android.presentation.course_list.CourseListQueryView
import org.stepik.android.presentation.course_list.CourseListView
import org.stepik.android.presentation.filter.FilterQueryView
import org.stepik.android.view.course.mapper.DisplayPriceMapper
import org.stepik.android.view.course_list.delegate.CourseContinueViewDelegate
import org.stepik.android.view.course_list.delegate.CourseListViewDelegate
import org.stepik.android.view.filter.ui.dialog.FilterSearchBottomSheetDialogFragment
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.app.core.model.PaginationDirection
import ru.nobird.android.view.base.ui.extension.argument
import ru.nobird.android.view.base.ui.extension.setOnPaginationListener
import ru.nobird.android.view.base.ui.extension.showIfNotExists
import javax.inject.Inject

class CourseListQueryFragment :
    Fragment(R.layout.fragment_course_list),
    CourseListQueryView,
    FilterQueryView,
    FilterSearchBottomSheetDialogFragment.Callback {
    private val binding: FragmentCourseListBinding by viewBinding(FragmentCourseListBinding::bind)

    companion object {
        fun newInstance(courseListTitle: String, courseListQuery: CourseListQuery): Fragment =
            CourseListQueryFragment().apply {
                this.courseListTitle = courseListTitle
                this.courseListQuery = courseListQuery
            }
    }

    private var menuDrawableRes: Int = R.drawable.ic_filter

    private var courseListTitle by argument<String>()
    private var courseListQuery by argument<CourseListQuery>()

    @Inject
    internal lateinit var analytic: Analytic

    @Inject
    internal lateinit var screenManager: ScreenManager

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    internal lateinit var defaultPromoCodeMapper: DefaultPromoCodeMapper

    @Inject
    internal lateinit var displayPriceMapper: DisplayPriceMapper

    private lateinit var courseListViewDelegate: CourseListViewDelegate
    private val courseListQueryPresenter: CourseListQueryPresenter by viewModels { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        injectComponent()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initCenteredToolbar(courseListTitle, true)

        with(binding.courseListCoursesRecycler) {
            layoutManager = GridLayoutManager(context, resources.getInteger(R.integer.course_list_columns))
            setOnPaginationListener { pageDirection ->
                if (pageDirection == PaginationDirection.NEXT) {
                    courseListQueryPresenter.fetchNextPage()
                }
            }
        }

        binding.courseListCoursesEmpty.goToCatalog.setOnClickListener { screenManager.showCatalog(requireContext()) }
        binding.courseListSwipeRefresh.setOnRefreshListener { courseListQueryPresenter.fetchCourses(courseListQuery = courseListQuery, forceUpdate = true) }
        binding.courseListCoursesLoadingErrorVertical.tryAgain.setOnClickListener { courseListQueryPresenter.fetchCourses(courseListQuery = courseListQuery, forceUpdate = true) }

        val viewStateDelegate = ViewStateDelegate<CourseListView.State>()
        viewStateDelegate.addState<CourseListView.State.Idle>()
        viewStateDelegate.addState<CourseListView.State.Loading>(binding.courseListCoursesRecycler)
        viewStateDelegate.addState<CourseListView.State.Content>(binding.courseListCoursesRecycler)
        viewStateDelegate.addState<CourseListView.State.Empty>(binding.courseListCoursesEmpty.root)
        viewStateDelegate.addState<CourseListView.State.NetworkError>(binding.courseListCoursesLoadingErrorVertical.root)

        courseListViewDelegate = CourseListViewDelegate(
            analytic = analytic,
            courseContinueViewDelegate = CourseContinueViewDelegate(
                activity = requireActivity(),
                analytic = analytic,
                screenManager = screenManager
            ),
            courseListSwipeRefresh = binding.courseListSwipeRefresh,
            courseItemsRecyclerView = binding.courseListCoursesRecycler,
            courseListViewStateDelegate = viewStateDelegate,
            onContinueCourseClicked = { courseListItem ->
                courseListQueryPresenter
                    .continueCourse(
                        course = courseListItem.course,
                        viewSource = CourseViewSource.Query(courseListQuery),
                        interactionSource = CourseContinueInteractionSource.COURSE_WIDGET
                    )
            },
            defaultPromoCodeMapper = defaultPromoCodeMapper,
            displayPriceMapper = displayPriceMapper
        )

        courseListQueryPresenter.fetchCourses(courseListQuery)
    }

    private fun injectComponent() {
        App.component()
            .courseListQueryComponentBuilder()
            .build()
            .inject(this)
    }

    override fun setState(state: CourseListQueryView.State) {
        val courseListState = (state as? CourseListQueryView.State.Data)?.courseListViewState ?: CourseListView.State.Idle
        courseListViewDelegate.setState(courseListState)
        (state as? CourseListQueryView.State.Data)?.let {
            menuDrawableRes = if (courseListQuery.filterQuery == it.courseListQuery.filterQuery) {
                R.drawable.ic_filter
            } else {
                R.drawable.ic_filter_active
            }
            requireActivity().invalidateOptionsMenu()
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
        courseListQueryPresenter.attachView(this)
    }

    override fun onStop() {
        courseListQueryPresenter.detachView(this)
        super.onStop()
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.findItem(R.id.course_list_filter).setIcon(menuDrawableRes)
        super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.course_list_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.course_list_filter -> {
                courseListQueryPresenter.onFilterMenuItemClicked()
                true
            }
            else ->
                super.onOptionsItemSelected(item)
        }

    override fun showFilterDialog(filterQuery: CourseListFilterQuery) {
        FilterSearchBottomSheetDialogFragment
            .newInstance(filterQuery)
            .showIfNotExists(childFragmentManager, FilterSearchBottomSheetDialogFragment.TAG)
    }

    override fun onSyncFilterQueryWithParent(filterQuery: CourseListFilterQuery) {
        courseListQueryPresenter.fetchCourses(
            courseListQuery = courseListQuery.copy(filterQuery = filterQuery),
            forceUpdate = true
        )
    }
}