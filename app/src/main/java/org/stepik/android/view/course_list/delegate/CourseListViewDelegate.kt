package org.stepik.android.view.course_list.delegate

import androidx.recyclerview.widget.RecyclerView
import org.stepic.droid.R
import org.stepic.droid.adaptive.util.AdaptiveCoursesResolver
import org.stepic.droid.ui.custom.StepikSwipeRefreshLayout
import org.stepic.droid.ui.util.snackbar
import org.stepik.android.domain.course_list.model.CourseListItem
import org.stepik.android.presentation.course_continue.CourseContinueView
import org.stepik.android.presentation.course_continue.delegate.CourseContinuePresenterDelegate
import org.stepik.android.presentation.course_continue.model.CourseContinueInteractionSource
import org.stepik.android.presentation.course_list.CourseListView
import org.stepik.android.view.course_list.ui.adapter.delegate.CourseListItemAdapterDelegate
import org.stepik.android.view.course_list.ui.adapter.delegate.CourseListPlaceHolderAdapterDelegate
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter

class CourseListViewDelegate(
    courseContinueViewDelegate: CourseContinueViewDelegate,
    adaptiveCoursesResolver: AdaptiveCoursesResolver,
    private val courseListSwipeRefresh: StepikSwipeRefreshLayout,
    private val courseItemsRecyclerView: RecyclerView,
    private val courseListViewStateDelegate: ViewStateDelegate<CourseListView.State>,
    private val courseListPresenter: CourseContinuePresenterDelegate
) : CourseListView, CourseContinueView by courseContinueViewDelegate {

    private var courseItemAdapter: DefaultDelegateAdapter<CourseListItem> = DefaultDelegateAdapter()

    init {
        courseItemAdapter += CourseListItemAdapterDelegate(
            adaptiveCoursesResolver,
            onItemClicked = courseContinueViewDelegate::onCourseClicked,
            onContinueCourseClicked = { courseListItem ->
                courseListPresenter.continueCourse(course = courseListItem.course, interactionSource = CourseContinueInteractionSource.COURSE_WIDGET)
            }
        )
        courseItemAdapter += CourseListPlaceHolderAdapterDelegate()
        courseItemsRecyclerView.adapter = courseItemAdapter
    }

    override fun setState(state: CourseListView.State) {
        courseListSwipeRefresh.isRefreshing = false
        courseListSwipeRefresh.isEnabled = (state is CourseListView.State.Content ||
                state is CourseListView.State.ContentLoading ||
                state is CourseListView.State.NetworkError)

        courseListViewStateDelegate.switchState(state)
        when (state) {
            is CourseListView.State.Loading -> {
                courseItemAdapter.items = listOf(
                    CourseListItem.PlaceHolder,
                    CourseListItem.PlaceHolder
                )
            }

            is CourseListView.State.Content ->
                courseItemAdapter.items = state.courseListItems

            is CourseListView.State.ContentLoading ->
                courseItemAdapter.items = state.courseListItems
        }
    }

    override fun showNetworkError() {
        courseItemsRecyclerView.snackbar(messageRes = R.string.connectionProblems)
    }
}