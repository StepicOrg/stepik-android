package org.stepik.android.view.course_list.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.empty_search.*
import kotlinx.android.synthetic.main.error_no_connection_with_button.view.*
import kotlinx.android.synthetic.main.fragment_course_list.*
import kotlinx.android.synthetic.main.view_catalog_search_toolbar.*
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.ui.custom.WrapContentLinearLayoutManager
import org.stepic.droid.ui.util.setOnPaginationListener
import org.stepik.android.domain.base.PaginationDirection
import org.stepik.android.domain.course_list.model.CourseListUserQuery
import org.stepik.android.domain.last_step.model.LastStep
import org.stepik.android.model.Course
import org.stepik.android.presentation.course_continue.model.CourseContinueInteractionSource
import org.stepik.android.presentation.course_list.CourseListUserPresenter
import org.stepik.android.presentation.course_list.CourseListUserView
import org.stepik.android.presentation.course_list.CourseListView
import org.stepik.android.presentation.course_list.model.CourseListUserType
import org.stepik.android.view.course_list.delegate.CourseContinueViewDelegate
import org.stepik.android.view.course_list.delegate.CourseListViewDelegate
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.android.view.base.ui.extension.argument
import javax.inject.Inject

class CourseListUserFragment : Fragment(R.layout.fragment_course_list), CourseListUserView {
    companion object {
        fun newInstance(courseListUserType: CourseListUserType): Fragment =
            CourseListUserFragment().apply {
                this.courseListUserType = courseListUserType
            }
    }

    private var courseListUserType by argument<CourseListUserType>()

    @Inject
    internal lateinit var analytic: Analytic

    @Inject
    internal lateinit var screenManager: ScreenManager

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var courseListViewDelegate: CourseListViewDelegate
    private lateinit var courseListPresenter: CourseListUserPresenter
    private lateinit var wrapperViewStateDelegate: ViewStateDelegate<CourseListUserView.State>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectComponent()

        courseListPresenter = ViewModelProviders
            .of(this, viewModelFactory)
            .get(CourseListUserPresenter::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        appBarLayout.isVisible = false
        courseListUserSkeleton.isVisible = true

        with(courseListCoursesRecycler) {
            layoutManager = WrapContentLinearLayoutManager(context)
            setOnPaginationListener { pageDirection ->
                if (pageDirection == PaginationDirection.NEXT) {
                    courseListPresenter.fetchNextPage()
                }
            }
        }

        goToCatalog.setOnClickListener { screenManager.showCatalog(requireContext()) }
        courseListSwipeRefresh.setOnRefreshListener {
            courseListPresenter.fetchUserCourses(courseListUserType = courseListUserType, forceUpdate = true)
        }
        courseListCoursesLoadingErrorVertical.tryAgain.setOnClickListener {
            courseListPresenter.fetchUserCourses(courseListUserType = courseListUserType, forceUpdate = true)
        }

        val viewStateDelegate = ViewStateDelegate<CourseListView.State>()
        viewStateDelegate.addState<CourseListView.State.Idle>()
        viewStateDelegate.addState<CourseListView.State.Loading>(courseListCoursesRecycler)
        viewStateDelegate.addState<CourseListView.State.Content>(courseListCoursesRecycler)
        viewStateDelegate.addState<CourseListView.State.Empty>(courseListCoursesEmpty)
        viewStateDelegate.addState<CourseListView.State.NetworkError>(courseListCoursesLoadingErrorVertical)

        courseListViewDelegate = CourseListViewDelegate(
            courseContinueViewDelegate = CourseContinueViewDelegate(
                activity = requireActivity(),
                analytic = analytic,
                screenManager = screenManager
            ),
            courseListSwipeRefresh = courseListSwipeRefresh,
            courseItemsRecyclerView = courseListCoursesRecycler,
            courseListViewStateDelegate = viewStateDelegate,
            onContinueCourseClicked = { courseListItem ->
                courseListPresenter.continueCourse(course = courseListItem.course, interactionSource = CourseContinueInteractionSource.COURSE_WIDGET)
            }
        )

        wrapperViewStateDelegate = ViewStateDelegate()
        wrapperViewStateDelegate.addState<CourseListUserView.State.Idle>()
        wrapperViewStateDelegate.addState<CourseListUserView.State.Loading>(courseListUserSkeleton)
        wrapperViewStateDelegate.addState<CourseListUserView.State.NetworkError>(courseListCoursesLoadingErrorVertical)
        wrapperViewStateDelegate.addState<CourseListUserView.State.Data>()
        courseListPresenter.fetchUserCourses(courseListUserType)
    }

    private fun injectComponent() {
        App.component()
            .courseListUserComponentBuilder()
            .build()
            .inject(this)
    }

    override fun setState(state: CourseListUserView.State) {
        val courseListState = (state as? CourseListUserView.State.Data)?.courseListViewState ?: CourseListView.State.Idle
        courseListViewDelegate.setState(courseListState)
        wrapperViewStateDelegate.switchState(state)
    }

    override fun showCourse(course: Course, isAdaptive: Boolean) {
        courseListViewDelegate.showCourse(course, isAdaptive)
    }

    override fun showSteps(course: Course, lastStep: LastStep) {
        courseListViewDelegate.showSteps(course, lastStep)
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
    }

    override fun onStop() {
        courseListPresenter.detachView(this)
        super.onStop()
    }
}