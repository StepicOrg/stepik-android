package org.stepik.android.view.course_list.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.databinding.FragmentCourseListBinding
import org.stepic.droid.model.CollectionDescriptionColors
import org.stepic.droid.ui.util.initCenteredToolbar
import org.stepic.droid.ui.util.setTitleToCenteredToolbar
import org.stepik.android.domain.course.analytic.CourseViewSource
import org.stepik.android.domain.course_payments.mapper.DefaultPromoCodeMapper
import org.stepik.android.domain.last_step.model.LastStep
import org.stepik.android.model.Course
import org.stepik.android.presentation.course_continue.model.CourseContinueInteractionSource
import org.stepik.android.presentation.course_list.CourseListCollectionPresenter
import org.stepik.android.presentation.course_list.CourseListCollectionView
import org.stepik.android.presentation.course_list.CourseListView
import org.stepik.android.view.base.ui.extension.enforceSingleScrollDirection
import org.stepik.android.view.catalog.mapper.CourseCountMapper
import org.stepik.android.view.course.mapper.DisplayPriceMapper
import org.stepik.android.view.course_list.delegate.CourseContinueViewDelegate
import org.stepik.android.view.course_list.delegate.CourseListViewDelegate
import org.stepik.android.view.course_list.ui.adapter.decorator.CourseListCollectionHeaderDecoration
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.app.core.model.PaginationDirection
import ru.nobird.android.view.base.ui.extension.argument
import ru.nobird.android.view.base.ui.extension.setOnPaginationListener
import javax.inject.Inject

class CourseListCollectionFragment : Fragment(R.layout.fragment_course_list), CourseListCollectionView {
    companion object {
        fun newInstance(courseCollectionId: Long): Fragment =
            CourseListCollectionFragment().apply {
                this.courseCollectionId = courseCollectionId
            }
    }

    private var courseCollectionId by argument<Long>()

    @Inject
    internal lateinit var analytic: Analytic

    @Inject
    internal lateinit var screenManager: ScreenManager

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    internal lateinit var courseCountMapper: CourseCountMapper

    @Inject
    internal lateinit var defaultPromoCodeMapper: DefaultPromoCodeMapper

    @Inject
    internal lateinit var displayPriceMapper: DisplayPriceMapper

    private val courseListBinding: FragmentCourseListBinding by viewBinding(FragmentCourseListBinding::bind)

    private lateinit var courseListViewDelegate: CourseListViewDelegate
    private val courseListPresenter: CourseListCollectionPresenter by viewModels { viewModelFactory }

    private lateinit var courseListCollectionHeaderDecoration: CourseListCollectionHeaderDecoration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectComponent()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initCenteredToolbar(R.string.catalog_title, true)

        courseListCollectionHeaderDecoration = CourseListCollectionHeaderDecoration()
        with(courseListBinding.courseListCoursesRecycler) {
            layoutManager = GridLayoutManager(context, resources.getInteger(R.integer.course_list_columns))
            itemAnimator = null
            addItemDecoration(courseListCollectionHeaderDecoration)
            setOnPaginationListener { pageDirection ->
                if (pageDirection == PaginationDirection.NEXT) {
                    courseListPresenter.fetchNextPage()
                }
            }
            enforceSingleScrollDirection()
        }

        courseListBinding.courseListCoursesEmpty.goToCatalog.setOnClickListener { screenManager.showCatalog(requireContext()) }
        courseListBinding.courseListSwipeRefresh.setOnRefreshListener { courseListPresenter.fetchCourses(courseCollectionId = courseCollectionId, forceUpdate = true) }
        courseListBinding.courseListCoursesLoadingErrorVertical.tryAgain.setOnClickListener { courseListPresenter.fetchCourses(courseCollectionId = courseCollectionId, forceUpdate = true) }

        val viewStateDelegate = ViewStateDelegate<CourseListView.State>()
        viewStateDelegate.addState<CourseListView.State.Idle>()
        viewStateDelegate.addState<CourseListView.State.Loading>(courseListBinding.courseListCoursesRecycler)
        viewStateDelegate.addState<CourseListView.State.Content>(courseListBinding.courseListCoursesRecycler)
        viewStateDelegate.addState<CourseListView.State.Empty>(courseListBinding.courseListCoursesEmpty.root)
        viewStateDelegate.addState<CourseListView.State.NetworkError>(courseListBinding.courseListCoursesLoadingErrorVertical.root)

        courseListViewDelegate = CourseListViewDelegate(
            analytic = analytic,
            courseContinueViewDelegate = CourseContinueViewDelegate(
                activity = requireActivity(),
                analytic = analytic,
                screenManager = screenManager
            ),
            courseListSwipeRefresh = courseListBinding.courseListSwipeRefresh,
            courseItemsRecyclerView = courseListBinding.courseListCoursesRecycler,
            courseListViewStateDelegate = viewStateDelegate,
            onContinueCourseClicked = { courseListItem ->
                courseListPresenter
                    .continueCourse(
                        course = courseListItem.course,
                        viewSource = CourseViewSource.Collection(courseCollectionId),
                        interactionSource = CourseContinueInteractionSource.COURSE_WIDGET
                    )
            },
            defaultPromoCodeMapper = defaultPromoCodeMapper,
            displayPriceMapper = displayPriceMapper,
            onCourseListClicked = { screenManager.showCoursesCollection(requireContext(), it.id) },
            onAuthorClick = { screenManager.openProfile(requireContext(), it) },
            courseCountMapper = courseCountMapper,
            isVerticalCourseCollection = true
        )
        courseListBinding.courseListCoursesRecycler.setPadding(0,
            resources.getDimensionPixelOffset(R.dimen.vertical_course_collection_padding),
            0,
            resources.getDimensionPixelOffset(R.dimen.vertical_course_collection_padding)
        )
        courseListPresenter.fetchCourses(courseCollectionId)
    }

    private fun injectComponent() {
        App.component()
            .courseListCollectionComponentBuilder()
            .build()
            .inject(this)
    }

    override fun setState(state: CourseListCollectionView.State) {
        when (state) {
            is CourseListCollectionView.State.Idle,
            is CourseListCollectionView.State.Loading -> {
                courseListViewDelegate.setState(CourseListView.State.Loading)
            }
            is CourseListCollectionView.State.Data -> {
                courseListCollectionHeaderDecoration.collectionDescriptionColors = CollectionDescriptionColors.ofCollection(state.courseCollection)
                courseListCollectionHeaderDecoration.headerText = state.courseCollection.description.takeIf { it.isNotEmpty() }

                setTitleToCenteredToolbar(state.courseCollection.title)
                courseListViewDelegate.setState(state.courseListViewState)
            }
            is CourseListCollectionView.State.NetworkError -> {
                courseListViewDelegate.setState(CourseListView.State.NetworkError)
            }
        }
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
