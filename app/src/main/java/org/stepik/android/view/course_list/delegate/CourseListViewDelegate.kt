package org.stepik.android.view.course_list.delegate

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import org.stepic.droid.R
import org.stepic.droid.adaptive.util.AdaptiveCoursesResolver
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.ui.util.snackbar
import org.stepik.android.domain.course_list.model.CourseListItem
import org.stepik.android.presentation.course_list.CourseListPresenter
import org.stepik.android.presentation.course_list.CourseListView
import org.stepik.android.view.course_list.ui.adapter.delegate.CourseListItemAdapterDelegate
import org.stepik.android.view.course_list.ui.adapter.delegate.CourseListPlaceHolderAdapterDelegate
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter
import timber.log.Timber

class CourseListViewDelegate(
    private val context: Context,
    private val analytic: Analytic,
    private val screenManager: ScreenManager,
    private val adaptiveCoursesResolver: AdaptiveCoursesResolver,
    private val isVertical: Boolean,
    private val courseItemsRecyclerView: RecyclerView,
    private val courseListPresenter: CourseListPresenter
) : CourseListView {

    private var courseItemAdapter: DefaultDelegateAdapter<CourseListItem> = DefaultDelegateAdapter()

    init {
        courseItemAdapter += CourseListItemAdapterDelegate(
            adaptiveCoursesResolver,
            onItemClicked = ::onCourseClicked,
            onContinueCourseClicked = { courseListItem ->
                // courseListPresenter.
                // profileCoursesPresenter.continueCourse(course = courseListItem.course, interactionSource = CourseContinueInteractionSource.COURSE_WIDGET)
            }
        )
        courseItemAdapter += CourseListPlaceHolderAdapterDelegate(isVertical)
        with(courseItemsRecyclerView) {
            adapter = courseItemAdapter
        }
    }

    override fun setState(state: CourseListView.State) {
        if (state is CourseListView.State.Content) {
            Timber.d("Items")
            courseItemAdapter.items = state.courseListItems
        }
    }

    override fun showNetworkError() {
        courseItemsRecyclerView.snackbar(messageRes = R.string.connectionProblems)
    }

    private fun onCourseClicked(courseListItem: CourseListItem.Data) {
        analytic.reportEvent(Analytic.Interaction.CLICK_COURSE)
        if (courseListItem.course.enrollment != 0L) {
            if (adaptiveCoursesResolver.isAdaptive(courseListItem.id)) {
//                screenManager.continueAdaptiveCourse(context, courseListItem.course)
            } else {
                screenManager.showCourseModules(context, courseListItem.course)
            }
        } else {
            screenManager.showCourseDescription(context, courseListItem.course)
        }
    }
}