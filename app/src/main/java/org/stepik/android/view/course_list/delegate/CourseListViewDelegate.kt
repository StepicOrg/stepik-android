package org.stepik.android.view.course_list.delegate

import androidx.recyclerview.widget.RecyclerView
import org.stepic.droid.R
import org.stepic.droid.adaptive.util.AdaptiveCoursesResolver
import org.stepic.droid.ui.util.snackbar
import org.stepik.android.domain.course_list.model.CourseListItem
import org.stepik.android.presentation.course_continue.CourseContinueView
import org.stepik.android.presentation.course_continue.model.CourseContinueInteractionSource
import org.stepik.android.presentation.course_list.CourseListPresenter
import org.stepik.android.presentation.course_list.CourseListView
import org.stepik.android.view.course_list.ui.adapter.delegate.CourseListItemAdapterDelegate
import org.stepik.android.view.course_list.ui.adapter.delegate.CourseListPlaceHolderAdapterDelegate
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter

class CourseListViewDelegate(
    courseContinueView: CourseContinueView,
    onCourseClicked: (CourseListItem.Data) -> Unit,
    adaptiveCoursesResolver: AdaptiveCoursesResolver,
    isVertical: Boolean,
    private val courseItemsRecyclerView: RecyclerView,
    private val courseListPresenter: CourseListPresenter
) : CourseListView, CourseContinueView by courseContinueView {

    private var courseItemAdapter: DefaultDelegateAdapter<CourseListItem> = DefaultDelegateAdapter()

    init {
        courseItemAdapter += CourseListItemAdapterDelegate(
            adaptiveCoursesResolver,
            onItemClicked = onCourseClicked,
            onContinueCourseClicked = { courseListItem ->
                courseListPresenter.continueCourse(course = courseListItem.course, interactionSource = CourseContinueInteractionSource.COURSE_WIDGET)
            }
        )
        courseItemAdapter += CourseListPlaceHolderAdapterDelegate(isVertical)
        with(courseItemsRecyclerView) {
            adapter = courseItemAdapter
        }
    }

    override fun setState(state: CourseListView.State) {
        if (state is CourseListView.State.Content) {
            courseItemAdapter.items = state.courseListItems
        }
    }

    override fun showNetworkError() {
        courseItemsRecyclerView.snackbar(messageRes = R.string.connectionProblems)
    }
}