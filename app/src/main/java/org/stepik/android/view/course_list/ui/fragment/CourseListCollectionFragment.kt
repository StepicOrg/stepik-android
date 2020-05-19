package org.stepik.android.view.course_list.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.empty_search.*
import kotlinx.android.synthetic.main.error_no_connection_with_button.*
import kotlinx.android.synthetic.main.fragment_course_list.*
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.model.CollectionDescriptionColors
import org.stepic.droid.ui.custom.WrapContentLinearLayoutManager
import org.stepic.droid.ui.util.initCenteredToolbar
import org.stepik.android.domain.course.analytic.CourseViewSource
import org.stepik.android.domain.last_step.model.LastStep
import org.stepik.android.model.Course
import org.stepik.android.model.CourseCollection
import org.stepik.android.presentation.course_continue.model.CourseContinueInteractionSource
import org.stepik.android.presentation.course_list.CourseListCollectionPresenter
import org.stepik.android.presentation.course_list.CourseListCollectionView
import org.stepik.android.presentation.course_list.CourseListView
import org.stepik.android.view.course_list.delegate.CourseContinueViewDelegate
import org.stepik.android.view.course_list.delegate.CourseListViewDelegate
import org.stepik.android.view.course_list.ui.adapter.decorator.CourseListCollectionHeaderDecoration
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.android.view.base.ui.extension.argument
import javax.inject.Inject

class CourseListCollectionFragment : Fragment(R.layout.fragment_course_list), CourseListCollectionView {
    companion object {
        fun newInstance(courseCollection: CourseCollection, collectionDescriptionColors: CollectionDescriptionColors): Fragment =
            CourseListCollectionFragment().apply {
                this.courseCollection = courseCollection
                this.collectionDescriptionColors = collectionDescriptionColors
            }
    }

    private var courseCollection by argument<CourseCollection>()
    private var collectionDescriptionColors by argument<CollectionDescriptionColors>()

    @Inject
    internal lateinit var analytic: Analytic

    @Inject
    internal lateinit var screenManager: ScreenManager

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var courseListViewDelegate: CourseListViewDelegate
    private lateinit var courseListPresenter: CourseListCollectionPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectComponent()

        courseListPresenter = ViewModelProviders
            .of(this, viewModelFactory)
            .get(CourseListCollectionPresenter::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initCenteredToolbar(courseCollection.title, true)
        with(courseListCoursesRecycler) {
            layoutManager = WrapContentLinearLayoutManager(context)
        }

        courseListCoursesRecycler.itemAnimator = null
        courseListCoursesRecycler.addItemDecoration(
            CourseListCollectionHeaderDecoration(
                courseCollection.description,
                collectionDescriptionColors
            )
        )

        goToCatalog.setOnClickListener { screenManager.showCatalog(requireContext()) }
        courseListSwipeRefresh.setOnRefreshListener { courseListPresenter.fetchCourses(courseCollection = courseCollection, forceUpdate = true) }
        tryAgain.setOnClickListener { courseListPresenter.fetchCourses(courseCollection = courseCollection, forceUpdate = true) }

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
                courseListPresenter
                    .continueCourse(
                        course = courseListItem.course,
                        viewSource = CourseViewSource.Collection(courseCollection.id),
                        interactionSource = CourseContinueInteractionSource.COURSE_WIDGET
                    )
            }
        )

        courseListPresenter.fetchCourses(courseCollection)
    }

    private fun injectComponent() {
        App.component()
            .courseListCollectionComponentBuilder()
            .build()
            .inject(this)
    }

    override fun setState(state: CourseListCollectionView.State) {
        val courseListState = (state as? CourseListCollectionView.State.Data)?.courseListViewState ?: CourseListView.State.Idle
        courseListViewDelegate.setState(courseListState)
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