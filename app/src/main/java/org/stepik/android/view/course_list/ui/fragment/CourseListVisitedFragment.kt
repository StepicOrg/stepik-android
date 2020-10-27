package org.stepik.android.view.course_list.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.item_course_list.*
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.analytic.experiments.InAppPurchaseSplitTest
import org.stepic.droid.base.App
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.ui.util.CoursesSnapHelper
import org.stepik.android.domain.course.analytic.CourseViewSource
import org.stepik.android.presentation.course_continue.model.CourseContinueInteractionSource
import org.stepik.android.presentation.course_list.CourseListView
import org.stepik.android.presentation.course_list.CourseListVisitedPresenter
import org.stepik.android.view.course_list.delegate.CourseContinueViewDelegate
import org.stepik.android.view.course_list.delegate.CourseListViewDelegate
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import javax.inject.Inject

class CourseListVisitedFragment : Fragment(R.layout.item_course_list) {
    companion object {
        fun newInstance(): Fragment =
            CourseListVisitedFragment()
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
    private val courseListVisitedPresenter: CourseListVisitedPresenter by viewModels { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectComponent()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        coursesCarouselCount.isVisible = false
        courseListTitle.text = resources.getString(R.string.visited_courses_title)

        with(courseListCoursesRecycler) {
            val rowCount = 1
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            itemAnimator?.changeDuration = 0
            val snapHelper = CoursesSnapHelper(rowCount)
            snapHelper.attachToRecyclerView(this)
        }

        // TODO Full screen

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
                courseListVisitedPresenter
                    .continueCourse(
                        course = courseListItem.course,
                        viewSource = CourseViewSource.Visited,
                        interactionSource = CourseContinueInteractionSource.COURSE_WIDGET
                    )
            },
            isHandleInAppPurchase = inAppPurchaseSplitTest.currentGroup.isInAppPurchaseActive,
            itemAdapterDelegateType = CourseListViewDelegate.ItemAdapterDelegateType.VISITED
        )

        courseListVisitedPresenter.fetchCourses()
    }

    private fun injectComponent() {
        App.component()
            .courseListVisitedComponentBuilder()
            .build()
            .inject(this)
    }

    override fun onStart() {
        super.onStart()
        courseListVisitedPresenter.attachView(courseListViewDelegate)
    }

    override fun onStop() {
        courseListVisitedPresenter.detachView(courseListViewDelegate)
        super.onStop()
    }
}