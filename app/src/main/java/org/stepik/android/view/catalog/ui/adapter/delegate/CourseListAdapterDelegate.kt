package org.stepik.android.view.catalog.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import kotlinx.android.synthetic.main.fragment_course_list.*
import kotlinx.android.synthetic.main.fragment_course_list.view.*
import kotlinx.android.synthetic.main.fragment_course_list.view.courseListPlaceholder
import org.stepic.droid.R
import org.stepic.droid.adaptive.util.AdaptiveCoursesResolver
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.core.ScreenManager
import org.stepik.android.presentation.base.PresenterViewHolder
import org.stepik.android.presentation.catalog.CatalogItem
import org.stepik.android.presentation.course_continue.CourseContinueView
import org.stepik.android.presentation.course_list.CourseListCollectionPresenter
import org.stepik.android.presentation.course_list.CourseListView
import org.stepik.android.view.course_list.delegate.CourseContinueViewDelegate
import org.stepik.android.view.course_list.delegate.CourseListPlaceholderDelegate
import org.stepik.android.view.course_list.delegate.CourseListViewDelegate
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class CourseListAdapterDelegate(
    private val activity: FragmentActivity,
    private val analytic: Analytic,
    private val screenManager: ScreenManager,
    private val adaptiveCoursesResolver: AdaptiveCoursesResolver
) : AdapterDelegate<CatalogItem, DelegateViewHolder<CatalogItem>>() {
    override fun isForViewType(position: Int, data: CatalogItem): Boolean =
        data is CourseListCollectionPresenter

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CatalogItem> {
        val view = createView(parent, R.layout.fragment_course_list)
        val viewStateDelegate =  ViewStateDelegate<CourseListView.State>()
        return CourseCollectionViewHolder(
            root = view,
            courseListViewDelegate = CourseListViewDelegate(
                courseContinueViewDelegate = CourseContinueViewDelegate(
                    activity = activity,
                    analytic = analytic,
                    screenManager = screenManager,
                    adaptiveCoursesResolver = adaptiveCoursesResolver
                ),
                adaptiveCoursesResolver = adaptiveCoursesResolver,
                courseListTitleContainer = view.courseListTitleContainer,
                courseItemsRecyclerView = view.courseListCoursesRecycler,
                courseListViewStateDelegate = viewStateDelegate,
                onContinueCourseClicked = { courseListItem ->
//                courseListPresenter.continueCourse(course = courseListItem.course, interactionSource = CourseContinueInteractionSource.COURSE_WIDGET)
                },
                courseListPlaceholderDelegate = CourseListPlaceholderDelegate(
                    placeholderTextView = view.courseListPlaceholder,
                    emptyMessageRes = R.string.empty_courses_popular,
                    emptyListener = { screenManager.showCatalog(view.context) },
                    errorListener = { /*itemData?.fetchCourses(courseListQuery, forceUpdate = true) */ }
                )
            )
        ) as DelegateViewHolder<CatalogItem>
    }

    private class CourseCollectionViewHolder(
        root: View,
        courseListViewDelegate: CourseListViewDelegate
    ) : PresenterViewHolder<CourseListView, CourseListCollectionPresenter>(root), CourseListView by courseListViewDelegate  {

        override fun attachView(data: CourseListCollectionPresenter) {
            data.attachView(this)
        }

        override fun detachView(data: CourseListCollectionPresenter) {
            data.detachView(this)
        }
    }
}