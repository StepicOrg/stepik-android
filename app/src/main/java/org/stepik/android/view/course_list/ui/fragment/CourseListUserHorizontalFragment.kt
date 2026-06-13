package org.stepik.android.view.course_list.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import org.stepic.droid.R
import org.stepic.droid.databinding.FragmentUserCourseListBinding
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.analytic.experiments.OnboardingSplitTestVersion2
import org.stepic.droid.base.App
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.util.defaultLocale
import org.stepik.android.domain.course.analytic.CourseViewSource
import org.stepik.android.domain.course_list.model.UserCourseQuery
import org.stepik.android.domain.course_payments.mapper.DefaultPromoCodeMapper
import org.stepik.android.domain.last_step.model.LastStep
import org.stepik.android.model.Course
import org.stepik.android.presentation.course_continue.model.CourseContinueInteractionSource
import org.stepik.android.presentation.course_list.CourseListUserPresenter
import org.stepik.android.presentation.course_list.CourseListUserView
import org.stepik.android.presentation.course_list.CourseListView
import org.stepik.android.view.base.ui.adapter.layoutmanager.TableLayoutManager
import org.stepik.android.view.course.mapper.DisplayPriceMapper
import org.stepik.android.view.course_list.delegate.CourseContinueViewDelegate
import org.stepik.android.view.course_list.delegate.CourseListViewDelegate
import org.stepik.android.view.course_list.resolver.TableLayoutHorizontalSpanCountResolver
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.app.core.model.PaginationDirection
import ru.nobird.android.view.base.ui.extension.setOnPaginationListener
import javax.inject.Inject

class CourseListUserHorizontalFragment : Fragment(R.layout.fragment_user_course_list), CourseListUserView {
    private val binding: FragmentUserCourseListBinding by viewBinding(FragmentUserCourseListBinding::bind)

    companion object {
        fun newInstance(): Fragment =
            CourseListUserHorizontalFragment()

        private val OnboardingSplitTestVersion2.Group.isPersonalized: Boolean
            get() = this == OnboardingSplitTestVersion2.Group.Personalized || this == OnboardingSplitTestVersion2.Group.ControlPersonalized

        private const val RUSSIAN_LANGUAGE_CODE = "ru"
    }

    @Inject
    internal lateinit var analytic: Analytic

    @Inject
    internal lateinit var screenManager: ScreenManager

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    internal lateinit var defaultPromoCodeMapper: DefaultPromoCodeMapper

    @Inject
    internal lateinit var displayPriceMapper: DisplayPriceMapper

    @Inject
    internal lateinit var onboardingSplitTestVersion2: OnboardingSplitTestVersion2

    @Inject
    internal lateinit var tableLayoutHorizontalSpanCountResolver: TableLayoutHorizontalSpanCountResolver

    private lateinit var courseListViewDelegate: CourseListViewDelegate
    private val courseListPresenter: CourseListUserPresenter by viewModels { viewModelFactory }
    private lateinit var wrapperViewStateDelegate: ViewStateDelegate<CourseListUserView.State>
    private lateinit var tableLayoutManager: TableLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectComponent()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.containerTitle.text = resources.getString(R.string.course_list_user_courses_title)
        val rowCount = resources.getInteger(R.integer.course_list_rows)
        val columnsCount = resources.getInteger(R.integer.course_list_columns)
        tableLayoutManager = TableLayoutManager(requireContext(), columnsCount, rowCount, RecyclerView.HORIZONTAL, false)

        with(binding.courseListCoursesRecycler) {
            layoutManager = tableLayoutManager
            itemAnimator?.changeDuration = 0
            val snapHelper = LinearSnapHelper()
            snapHelper.attachToRecyclerView(this)
            setOnPaginationListener { pageDirection ->
                if (pageDirection == PaginationDirection.NEXT) {
                    courseListPresenter.fetchNextPage()
                }
            }
        }

        binding.catalogBlockContainer.setOnClickListener { screenManager.showUserCourses(requireContext()) }

        /**
         * Empty user courses action
         */
        val isShowPersonalizedOnboarding =
            resources.configuration.defaultLocale.language == RUSSIAN_LANGUAGE_CODE &&
                    onboardingSplitTestVersion2.currentGroup.isPersonalized

        val userCoursesListEmptyActionListener = View.OnClickListener {
            if (isShowPersonalizedOnboarding) {
                screenManager.showPersonalizedOnboarding(requireContext())
            } else {
                screenManager.showCatalog(requireContext())
            }
        }
        val userCoursesListEmptyActionText =
            if (isShowPersonalizedOnboarding) {
                R.string.onboarding_restart_action
            } else {
                R.string.user_courses_catalog_action_message
            }

        with(binding.courseListPlaceholderEmpty.userCoursesListEmptyAction) {
            setOnClickListener(userCoursesListEmptyActionListener)
            setText(userCoursesListEmptyActionText)
        }
        with(binding.courseListPlaceholderEmptyWrapper.userCoursesListEmptyAction) {
            setOnClickListener(userCoursesListEmptyActionListener)
            setText(userCoursesListEmptyActionText)
        }

        binding.courseListPlaceholderNoConnection
            .userCoursesListNetworkErrorAction
            .setOnClickListener {
                setDataToPresenter(forceUpdate = true)
            }
        binding.courseListPlaceholderNoConnectionWrapper
            .userCoursesListNetworkErrorAction
            .setOnClickListener {
                setDataToPresenter(forceUpdate = true)
            }

        val viewStateDelegate = ViewStateDelegate<CourseListView.State>()

        viewStateDelegate.addState<CourseListView.State.Idle>(binding.catalogBlockContainer, binding.containerTitle)
        viewStateDelegate.addState<CourseListView.State.Loading>(binding.catalogBlockContainer, binding.containerTitle, binding.courseListCoursesRecycler)
        viewStateDelegate.addState<CourseListView.State.Content>(binding.catalogBlockContainer, binding.containerTitle, binding.containerCarouselCount, binding.containerViewAll, binding.courseListCoursesRecycler)
        viewStateDelegate.addState<CourseListView.State.Empty>(binding.catalogBlockContainer, binding.containerTitle, binding.courseListPlaceholderEmpty.root)
        viewStateDelegate.addState<CourseListView.State.NetworkError>(binding.catalogBlockContainer, binding.containerTitle, binding.courseListPlaceholderNoConnection.root)

        courseListViewDelegate = CourseListViewDelegate(
            analytic = analytic,
            courseContinueViewDelegate = CourseContinueViewDelegate(
                activity = requireActivity(),
                analytic = analytic,
                screenManager = screenManager
            ),
            courseItemsRecyclerView = binding.courseListCoursesRecycler,
            courseListViewStateDelegate = viewStateDelegate,
            onContinueCourseClicked = { courseListItem ->
                courseListPresenter
                    .continueCourse(
                        course = courseListItem.course,
                        viewSource = CourseViewSource.MyCourses,
                        interactionSource = CourseContinueInteractionSource.COURSE_WIDGET
                    )
            },
            defaultPromoCodeMapper = defaultPromoCodeMapper,
            displayPriceMapper = displayPriceMapper
        )

        wrapperViewStateDelegate = ViewStateDelegate()
        wrapperViewStateDelegate.addState<CourseListUserView.State.Idle>()
        wrapperViewStateDelegate.addState<CourseListUserView.State.Loading>(binding.courseListUserSkeleton)
        wrapperViewStateDelegate.addState<CourseListUserView.State.EmptyLogin>(binding.courseListPlaceholderEmptyWrapper.root)
        wrapperViewStateDelegate.addState<CourseListUserView.State.NetworkError>(binding.courseListPlaceholderNoConnectionWrapper.root)
        wrapperViewStateDelegate.addState<CourseListUserView.State.Data>()

        setDataToPresenter()
    }

    private fun injectComponent() {
        App.component()
            .courseListUserComponentBuilder()
            .build()
            .inject(this)
    }

    private fun setDataToPresenter(forceUpdate: Boolean = false) {
        courseListPresenter.fetchUserCourses(UserCourseQuery(page = 1, isArchived = false), forceUpdate)
    }

    override fun setState(state: CourseListUserView.State) {
        binding.catalogBlockContainer.isEnabled = (state as? CourseListUserView.State.Data)?.courseListViewState is CourseListView.State.Content
        if (state is CourseListUserView.State.Data) {
            binding.containerCarouselCount.text = requireContext().resources.getQuantityString(
                R.plurals.course_count,
                state.userCourses.size,
                state.userCourses.size
            )
            tableLayoutHorizontalSpanCountResolver.resolveSpanCount(state.userCourses.size).let { resolvedSpanCount ->
                if (tableLayoutManager.spanCount != resolvedSpanCount) {
                    tableLayoutManager.spanCount = resolvedSpanCount
                }
            }
        }
        val courseListState = (state as? CourseListUserView.State.Data)?.courseListViewState ?: CourseListView.State.Idle
        courseListViewDelegate.setState(courseListState)
        wrapperViewStateDelegate.switchState(state)
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