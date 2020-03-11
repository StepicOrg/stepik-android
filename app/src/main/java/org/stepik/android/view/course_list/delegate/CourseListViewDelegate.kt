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
import org.stepik.android.view.course_list.ui.adapter.delegate.CourseListPlaceHolderTextAdapterDelegate
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter

class CourseListViewDelegate(
    courseItemViewDelegate: CourseItemViewDelegate,
    adaptiveCoursesResolver: AdaptiveCoursesResolver,
    private val courseItemsRecyclerView: RecyclerView,
    private val courseListPresenter: CourseListPresenter
) : CourseListView, CourseContinueView by courseItemViewDelegate {

    private var courseItemAdapter: DefaultDelegateAdapter<CourseListItem> = DefaultDelegateAdapter()

    init {
        courseItemAdapter += CourseListItemAdapterDelegate(
            adaptiveCoursesResolver,
            onItemClicked = courseItemViewDelegate::onCourseClicked,
            onContinueCourseClicked = { courseListItem ->
                courseListPresenter.continueCourse(course = courseListItem.course, interactionSource = CourseContinueInteractionSource.COURSE_WIDGET)
            }
        )
        courseItemAdapter += CourseListPlaceHolderAdapterDelegate()
        courseItemAdapter += CourseListPlaceHolderTextAdapterDelegate()
        courseItemsRecyclerView.adapter = courseItemAdapter
    }

    override fun setState(state: CourseListView.State) {
        when (state) {
            is CourseListView.State.Loading -> {
                courseItemAdapter.items = listOf(
//                    CourseListItem.PlaceHolderText("JUEWIHDIEHDIEHDEIHDIEHDEIDEHU"),
                    CourseListItem.PlaceHolder,
                    CourseListItem.PlaceHolder
                )
            }
            is CourseListView.State.Content ->
                courseItemAdapter.items = state.courseListItems
//                courseItemAdapter.items = listOf(CourseListItem.PlaceHolderText("JUEWIHDIEHDIEHDEIHDIEHDEIDEHU")) + state.courseListItems
        }
    }

    override fun showNetworkError() {
        courseItemsRecyclerView.snackbar(messageRes = R.string.connectionProblems)
    }
}