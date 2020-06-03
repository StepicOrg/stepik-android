package org.stepik.android.view.course_list.delegate

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_course_list.view.*
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.ui.custom.StepikSwipeRefreshLayout
import org.stepic.droid.ui.util.snackbar
import org.stepik.android.domain.course_list.model.CourseListItem
import org.stepik.android.presentation.course_continue.CourseContinueView
import org.stepik.android.presentation.course_list.CourseListView
import org.stepik.android.view.course_list.ui.adapter.delegate.CourseListItemAdapterDelegate
import org.stepik.android.view.course_list.ui.adapter.delegate.CourseListPlaceHolderAdapterDelegate
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter

class CourseListViewDelegate(
    analytic: Analytic,
    courseContinueViewDelegate: CourseContinueViewDelegate,
    courseListTitleContainer: View? = null,
    private val courseListSwipeRefresh: StepikSwipeRefreshLayout? = null,
    private val courseItemsRecyclerView: RecyclerView,
    private val courseListViewStateDelegate: ViewStateDelegate<CourseListView.State>,
    private val onContinueCourseClicked: (CourseListItem.Data) -> Unit
) : CourseListView, CourseContinueView by courseContinueViewDelegate {

    private val courseListCounter = courseListTitleContainer?.coursesCarouselCount
    private var courseItemAdapter: DefaultDelegateAdapter<CourseListItem> = DefaultDelegateAdapter()

    init {
        courseItemAdapter += CourseListItemAdapterDelegate(
            analytic,
            onItemClicked = courseContinueViewDelegate::onCourseClicked,
            onContinueCourseClicked = onContinueCourseClicked
        )
        courseItemAdapter += CourseListPlaceHolderAdapterDelegate()
        courseItemsRecyclerView.adapter = courseItemAdapter
    }

    override fun setState(state: CourseListView.State) {
        courseListSwipeRefresh?.isRefreshing = false
        courseListSwipeRefresh?.isEnabled = (state is CourseListView.State.Content ||
                state is CourseListView.State.Empty ||
                state is CourseListView.State.NetworkError)

        courseListViewStateDelegate.switchState(state)
        when (state) {
            is CourseListView.State.Loading -> {
                courseItemAdapter.items = listOf(
                    CourseListItem.PlaceHolder(),
                    CourseListItem.PlaceHolder()
                )
            }

            is CourseListView.State.Content -> {
                courseItemAdapter.items = state.courseListItems
                courseListCounter?.text =
                    courseItemsRecyclerView.context.resources.getQuantityString(
                        R.plurals.course_count,
                        state.courseListDataItems.size,
                        state.courseListDataItems.size
                    )

                /**
                 * notify is necessary, because margins don't get recalculated after adding loading placeholder
                 */
                val size = state.courseListItems.size
                if (size > 2 &&
                    (courseItemsRecyclerView.layoutManager as? LinearLayoutManager)?.orientation == LinearLayoutManager.HORIZONTAL) {
                    courseItemAdapter.notifyItemChanged(size - 2)
                    courseItemAdapter.notifyItemChanged(size - 3)
                }
            }
        }
    }

    override fun showNetworkError() {
        courseItemsRecyclerView.snackbar(messageRes = R.string.connectionProblems)
    }
}