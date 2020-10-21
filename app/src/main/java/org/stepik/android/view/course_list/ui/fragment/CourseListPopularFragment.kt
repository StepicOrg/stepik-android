package org.stepik.android.view.course_list.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_course_list.*
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.analytic.experiments.InAppPurchaseSplitTest
import org.stepic.droid.base.App
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.ui.util.CoursesSnapHelper
import org.stepic.droid.ui.util.setOnPaginationListener
import org.stepik.android.domain.base.PaginationDirection
import org.stepik.android.domain.course.analytic.CourseViewSource
import org.stepik.android.domain.course_list.model.CourseListQuery
import org.stepik.android.domain.filter.model.CourseListFilterQuery
import org.stepik.android.domain.last_step.model.LastStep
import org.stepik.android.model.Course
import org.stepik.android.presentation.course_continue.model.CourseContinueInteractionSource
import org.stepik.android.presentation.course_list.CourseListQueryPresenter
import org.stepik.android.presentation.course_list.CourseListQueryView
import org.stepik.android.presentation.course_list.CourseListView
import org.stepik.android.view.base.ui.adapter.layoutmanager.TableLayoutManager
import org.stepik.android.view.course_list.delegate.CourseContinueViewDelegate
import org.stepik.android.view.course_list.delegate.CourseListViewDelegate
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import javax.inject.Inject

class CourseListPopularFragment : Fragment(R.layout.item_course_list), CourseListQueryView {
    companion object {
        fun newInstance(): Fragment =
            CourseListPopularFragment()
    }

    @Inject
    internal lateinit var analytic: Analytic

    @Inject
    internal lateinit var screenManager: ScreenManager

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    internal lateinit var sharedPreferenceHelper: SharedPreferenceHelper

    @Inject
    internal lateinit var inAppPurchaseSplitTest: InAppPurchaseSplitTest

    private lateinit var courseListViewDelegate: CourseListViewDelegate
    private lateinit var courseListQueryPresenter: CourseListQueryPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectComponent()

        courseListQueryPresenter = ViewModelProviders
            .of(this, viewModelFactory)
            .get(CourseListQueryPresenter::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        coursesCarouselCount.isVisible = false
        courseListTitle.text = resources.getString(R.string.course_list_popular_toolbar_title)

        with(courseListCoursesRecycler) {
            val rowCount = resources.getInteger(R.integer.course_list_rows)
            val columnsCount = resources.getInteger(R.integer.course_list_columns)
            layoutManager = TableLayoutManager(context, columnsCount, rowCount, RecyclerView.HORIZONTAL, false)
            itemAnimator?.changeDuration = 0
            val snapHelper = CoursesSnapHelper(rowCount)
            snapHelper.attachToRecyclerView(this)
            setOnPaginationListener { pageDirection ->
                if (pageDirection == PaginationDirection.NEXT) {
                    courseListQueryPresenter.fetchNextPage()
                }
            }
        }

        val courseListQuery = CourseListQuery(
            page = 1,
            order = CourseListQuery.Order.ACTIVITY_DESC,
            isCataloged = true,
            filterQuery = CourseListFilterQuery(language = sharedPreferenceHelper.languageForFeatured)
        )

        courseListTitleContainer.setOnClickListener {
            screenManager.showCoursesByQuery(
                requireContext(),
                resources.getString(R.string.course_list_popular_toolbar_title),
                courseListQuery
            )
        }

        courseListPlaceholderEmpty.setOnClickListener { screenManager.showCatalog(requireContext()) }
        courseListPlaceholderEmpty.setPlaceholderText(R.string.empty_courses_popular)
        courseListPlaceholderNoConnection.setOnClickListener { courseListQueryPresenter.fetchCourses(courseListQuery = courseListQuery, forceUpdate = true) }
        courseListPlaceholderNoConnection.setText(R.string.internet_problem)

        val viewStateDelegate = ViewStateDelegate<CourseListView.State>()

        viewStateDelegate.addState<CourseListView.State.Idle>(courseListTitleContainer)
        viewStateDelegate.addState<CourseListView.State.Loading>(courseListTitleContainer, courseListCoursesRecycler)
        viewStateDelegate.addState<CourseListView.State.Content>(courseListTitleContainer, courseListCoursesRecycler)
        viewStateDelegate.addState<CourseListView.State.Empty>(courseListPlaceholderEmpty)
        viewStateDelegate.addState<CourseListView.State.NetworkError>(courseListPlaceholderNoConnection)

        courseListViewDelegate = CourseListViewDelegate(
            analytic = analytic,
            courseContinueViewDelegate = CourseContinueViewDelegate(
                activity = requireActivity(),
                analytic = analytic,
                screenManager = screenManager
            ),
            courseListTitleContainer = courseListTitleContainer,
            courseItemsRecyclerView = courseListCoursesRecycler,
            courseListViewStateDelegate = viewStateDelegate,
            onContinueCourseClicked = { courseListItem ->
                courseListQueryPresenter
                    .continueCourse(
                        course = courseListItem.course,
                        viewSource = CourseViewSource.Query(courseListQuery),
                        interactionSource = CourseContinueInteractionSource.COURSE_WIDGET
                    )
            },
            isHandleInAppPurchase = inAppPurchaseSplitTest.currentGroup.isInAppPurchaseActive
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
        if (courseListState == CourseListView.State.Empty) {
            analytic.reportEvent(Analytic.Error.FEATURED_EMPTY)
        }
        courseListViewDelegate.setState(courseListState)
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
}