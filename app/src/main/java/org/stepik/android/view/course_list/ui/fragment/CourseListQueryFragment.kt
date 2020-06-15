package org.stepik.android.view.course_list.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.empty_search.*
import kotlinx.android.synthetic.main.error_no_connection_with_button.*
import kotlinx.android.synthetic.main.fragment_course_list.*
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.ui.custom.WrapContentLinearLayoutManager
import org.stepic.droid.ui.util.initCenteredToolbar
import org.stepic.droid.ui.util.setOnPaginationListener
import org.stepik.android.domain.base.PaginationDirection
import org.stepik.android.domain.course.analytic.CourseViewSource
import org.stepik.android.domain.course_list.model.CourseListQuery
import org.stepik.android.domain.last_step.model.LastStep
import org.stepik.android.model.Course
import org.stepik.android.presentation.course_continue.model.CourseContinueInteractionSource
import org.stepik.android.presentation.course_list.CourseListQueryPresenter
import org.stepik.android.presentation.course_list.CourseListQueryView
import org.stepik.android.presentation.course_list.CourseListView
import org.stepik.android.view.course_list.delegate.CourseContinueViewDelegate
import org.stepik.android.view.course_list.delegate.CourseListViewDelegate
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.android.view.base.ui.extension.argument
import javax.inject.Inject

class CourseListQueryFragment : Fragment(R.layout.fragment_course_list), CourseListQueryView {
    companion object {
        fun newInstance(courseListTitle: String, courseListQuery: CourseListQuery): Fragment =
            CourseListQueryFragment().apply {
                this.courseListTitle = courseListTitle
                this.courseListQuery = courseListQuery
            }
    }

    private var courseListTitle by argument<String>()
    private var courseListQuery by argument<CourseListQuery>()

    @Inject
    internal lateinit var analytic: Analytic

    @Inject
    internal lateinit var screenManager: ScreenManager

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

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

        initCenteredToolbar(courseListTitle, true)
        with(courseListCoursesRecycler) {
            layoutManager = GridLayoutManager(context, resources.getInteger(R.integer.course_list_columns))
            setOnPaginationListener { pageDirection ->
                if (pageDirection == PaginationDirection.NEXT) {
                    courseListQueryPresenter.fetchNextPage()
                }
            }
        }

        goToCatalog.setOnClickListener { screenManager.showCatalog(requireContext()) }
        courseListSwipeRefresh.setOnRefreshListener { courseListQueryPresenter.fetchCourses(courseListQuery = courseListQuery, forceUpdate = true) }
        tryAgain.setOnClickListener { courseListQueryPresenter.fetchCourses(courseListQuery = courseListQuery, forceUpdate = true) }

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
                courseListQueryPresenter
                    .continueCourse(
                        course = courseListItem.course,
                        viewSource = CourseViewSource.Query(courseListQuery),
                        interactionSource = CourseContinueInteractionSource.COURSE_WIDGET
                    )
            }
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