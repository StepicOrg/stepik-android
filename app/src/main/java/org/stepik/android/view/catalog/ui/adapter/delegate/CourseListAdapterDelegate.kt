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
import org.stepik.android.presentation.course_continue.model.CourseContinueInteractionSource
import org.stepik.android.presentation.course_list.CourseListCollectionPresenter
import org.stepik.android.presentation.course_list.CourseListView
import org.stepik.android.view.course_list.delegate.CourseContinueViewDelegate
import org.stepik.android.view.course_list.delegate.CourseListPlaceholderDelegate
import org.stepik.android.view.course_list.delegate.CourseListViewDelegate
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class CourseListAdapterDelegate(
    private val screenManager: ScreenManager,
    private val adaptiveCoursesResolver: AdaptiveCoursesResolver,
    private val courseContinueViewDelegate: CourseContinueViewDelegate
) : AdapterDelegate<CatalogItem, DelegateViewHolder<CatalogItem>>() {
    override fun isForViewType(position: Int, data: CatalogItem): Boolean =
        data is CourseListCollectionPresenter

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CatalogItem> =
        CourseCollectionViewHolder(createView(parent, R.layout.fragment_course_list)) as DelegateViewHolder<CatalogItem>

    private inner class CourseCollectionViewHolder(
        root: View
    ) : PresenterViewHolder<CourseListView, CourseListCollectionPresenter>(root) {

        private val viewStateDelegate = ViewStateDelegate<CourseListView.State>()

        private val delegate = CourseListViewDelegate(
            courseContinueViewDelegate = courseContinueViewDelegate,
            adaptiveCoursesResolver = adaptiveCoursesResolver,
            courseListTitleContainer = root.courseListTitleContainer,
            courseItemsRecyclerView = root.courseListCoursesRecycler,
            courseListViewStateDelegate = viewStateDelegate,
            onContinueCourseClicked = { courseListItem ->
                itemData?.continueCourse(course = courseListItem.course, interactionSource = CourseContinueInteractionSource.COURSE_WIDGET)
            },
            courseListPlaceholderDelegate = CourseListPlaceholderDelegate( // todo remove
                placeholderTextView = root.courseListPlaceholder,
                emptyMessageRes = R.string.empty_courses_popular,
                emptyListener = { screenManager.showCatalog(context) },
                errorListener = { /*itemData?.fetchCourses(courseListQuery, forceUpdate = true) */ }
            )
        )

        override fun attachView(data: CourseListCollectionPresenter) {
            data.attachView(delegate)
        }

        override fun detachView(data: CourseListCollectionPresenter) {
            data.detachView(delegate)
        }
    }
}