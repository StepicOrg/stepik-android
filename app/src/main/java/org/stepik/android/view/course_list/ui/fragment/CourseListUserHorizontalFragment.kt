package org.stepik.android.view.course_list.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_user_course_list.*
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.analytic.experiments.InAppPurchaseSplitTest
import org.stepic.droid.base.App
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.ui.activities.MainFeedActivity
import org.stepic.droid.ui.util.CoursesSnapHelper
import org.stepic.droid.ui.util.setOnPaginationListener
import org.stepik.android.domain.base.PaginationDirection
import org.stepik.android.domain.course.analytic.CourseViewSource
import org.stepik.android.domain.course_list.model.UserCourseQuery
import org.stepik.android.domain.last_step.model.LastStep
import org.stepik.android.model.Course
import org.stepik.android.presentation.course_continue.model.CourseContinueInteractionSource
import org.stepik.android.presentation.course_list.CourseListUserPresenter
import org.stepik.android.presentation.course_list.CourseListUserView
import org.stepik.android.presentation.course_list.CourseListView
import org.stepik.android.view.base.ui.adapter.layoutmanager.TableLayoutManager
import org.stepik.android.view.course_list.delegate.CourseContinueViewDelegate
import org.stepik.android.view.course_list.delegate.CourseListViewDelegate
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import javax.inject.Inject

class CourseListUserHorizontalFragment : Fragment(R.layout.fragment_user_course_list), CourseListUserView {
    companion object {
        fun newInstance(): Fragment =
            CourseListUserHorizontalFragment()
    }

    @Inject
    internal lateinit var analytic: Analytic

    @Inject
    internal lateinit var screenManager: ScreenManager

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    internal lateinit var inAppPurchaseSplitTest: InAppPurchaseSplitTest

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

        courseListTitle.text = resources.getString(R.string.course_list_user_courses_title)

        with(courseListCoursesRecycler) {
            val rowCount = resources.getInteger(R.integer.course_list_rows)
            val columnsCount = resources.getInteger(R.integer.course_list_columns)
            layoutManager = TableLayoutManager(context, columnsCount, rowCount, RecyclerView.HORIZONTAL, false)
            itemAnimator?.changeDuration = 0
            val snapHelper = CoursesSnapHelper(rowCount)
            snapHelper.attachToRecyclerView(this)
            setOnPaginationListener { pageDirection ->
                if (pageDirection == PaginationDirection.NEXT) {
                    courseListPresenter.fetchNextPage()
                }
            }
        }

        courseListTitleContainer.setOnClickListener { screenManager.showUserCourses(requireContext()) }
        courseListPlaceholderEmpty.setOnClickListener { screenManager.showCatalog(requireContext()) }
        courseListPlaceholderEmpty.setPlaceholderText(R.string.courses_carousel_my_courses_empty)
        courseListPlaceholderNoConnection.setOnClickListener {
            setDataToPresenter(forceUpdate = true)
        }
        courseListWrapperPlaceholderEmptyLogin.setOnClickListener {
            analytic.reportEvent(Analytic.Anonymous.AUTH_CENTER)
            screenManager.showLaunchScreen(context, true, MainFeedActivity.HOME_INDEX)
        }
        courseListPlaceholderNoConnection.setText(R.string.internet_problem)
        courseListWrapperPlaceholderEmptyLogin.setPlaceholderText(R.string.empty_courses_anonymous)

        val viewStateDelegate = ViewStateDelegate<CourseListView.State>()

        viewStateDelegate.addState<CourseListView.State.Idle>()
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
            courseItemsRecyclerView = courseListCoursesRecycler,
            courseListViewStateDelegate = viewStateDelegate,
            onContinueCourseClicked = { courseListItem ->
                courseListPresenter
                    .continueCourse(
                        course = courseListItem.course,
                        viewSource = CourseViewSource.MyCourses,
                        interactionSource = CourseContinueInteractionSource.COURSE_WIDGET
                    )
            },
            isHandleInAppPurchase = inAppPurchaseSplitTest.currentGroup.isInAppPurchaseActive
        )

        wrapperViewStateDelegate = ViewStateDelegate()
        wrapperViewStateDelegate.addState<CourseListUserView.State.Idle>()
        wrapperViewStateDelegate.addState<CourseListUserView.State.Loading>(courseListUserSkeleton)
        wrapperViewStateDelegate.addState<CourseListUserView.State.EmptyLogin>(courseListWrapperPlaceholderEmptyLogin)
        wrapperViewStateDelegate.addState<CourseListUserView.State.NetworkError>(courseListPlaceholderNoConnection)
        wrapperViewStateDelegate.addState<CourseListUserView.State.Data>()

        setDataToPresenter()
    }

    private fun injectComponent() {
        App.component()
            .courseListUserComponentBuilder()
            .build()
            .inject(this)
    }

    private fun setDataToPresenter(forceUpdate: Boolean = false) {
        courseListPresenter.fetchUserCourses(UserCourseQuery(page = 1, isArchived = false), forceUpdate)
    }

    override fun setState(state: CourseListUserView.State) {
        if (state is CourseListUserView.State.Data) {
            coursesCarouselCount.text = requireContext().resources.getQuantityString(
                R.plurals.course_count,
                state.userCourses.size,
                state.userCourses.size
            )
        }
        val courseListState = (state as? CourseListUserView.State.Data)?.courseListViewState ?: CourseListView.State.Idle
        courseListViewDelegate.setState(courseListState)
        wrapperViewStateDelegate.switchState(state)
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
    }

    override fun onStop() {
        courseListPresenter.detachView(this)
        super.onStop()
    }
}