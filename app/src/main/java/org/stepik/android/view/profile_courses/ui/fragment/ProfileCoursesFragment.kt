package org.stepik.android.view.profile_courses.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.error_no_connection_with_button_small.*
import kotlinx.android.synthetic.main.fragment_profile_courses.*
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.ui.decorators.RightMarginForLastItems
import org.stepic.droid.ui.dialogs.LoadingProgressDialogFragment
import org.stepic.droid.ui.util.CoursesSnapHelper
import org.stepic.droid.util.ProgressHelper
import org.stepik.android.domain.course_list.model.CourseListItem
import org.stepik.android.domain.last_step.model.LastStep
import org.stepik.android.model.Course
import org.stepik.android.presentation.course_continue.model.CourseContinueInteractionSource
import org.stepik.android.presentation.profile_courses.ProfileCoursesPresenter
import org.stepik.android.presentation.profile_courses.ProfileCoursesView
import org.stepik.android.view.course_list.delegate.CourseContinueViewDelegate
import org.stepik.android.view.course_list.ui.adapter.delegate.CourseListItemAdapterDelegate
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter
import ru.nobird.android.view.base.ui.extension.argument
import javax.inject.Inject
import kotlin.math.min

class ProfileCoursesFragment : Fragment(), ProfileCoursesView {
    companion object {
        private const val ROW_COUNT = 2

        fun newInstance(userId: Long): Fragment =
            ProfileCoursesFragment()
                .apply {
                    this.userId = userId
                }
    }

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    internal lateinit var analytic: Analytic

    @Inject
    internal lateinit var screenManager: ScreenManager

    private var userId by argument<Long>()

    private lateinit var profileCoursesPresenter: ProfileCoursesPresenter
    private lateinit var courseContinueViewDelegate: CourseContinueViewDelegate

    private lateinit var coursesAdapter: DefaultDelegateAdapter<CourseListItem>
    private lateinit var viewStateDelegate: ViewStateDelegate<ProfileCoursesView.State>

    private val progressDialogFragment: DialogFragment =
        LoadingProgressDialogFragment.newInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        injectComponent()

        profileCoursesPresenter = ViewModelProviders
            .of(this, viewModelFactory)
            .get(ProfileCoursesPresenter::class.java)
        savedInstanceState?.let(profileCoursesPresenter::onRestoreInstanceState)

        courseContinueViewDelegate = CourseContinueViewDelegate(
            activity = requireActivity(),
            analytic = analytic,
            screenManager = screenManager
        )

        coursesAdapter = DefaultDelegateAdapter()

        coursesAdapter += CourseListItemAdapterDelegate(
            onItemClicked = courseContinueViewDelegate::onCourseClicked,
            onContinueCourseClicked = { courseListItem ->
                profileCoursesPresenter.continueCourse(course = courseListItem.course, interactionSource = CourseContinueInteractionSource.COURSE_WIDGET)
            }
        )
    }

    private fun injectComponent() {
        App.componentManager()
            .profileComponent(userId)
            .profileCoursesPresentationComponentBuilder()
            .build()
            .inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_profile_courses, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewStateDelegate = ViewStateDelegate()
        viewStateDelegate.addState<ProfileCoursesView.State.Idle>()
        viewStateDelegate.addState<ProfileCoursesView.State.SilentLoading>()
        viewStateDelegate.addState<ProfileCoursesView.State.Empty>()
        viewStateDelegate.addState<ProfileCoursesView.State.Error>(view, profileCoursesLoadingError)
        viewStateDelegate.addState<ProfileCoursesView.State.Content>(view, profileCoursesRecycler)

        setDataToPresenter()
        tryAgain.setOnClickListener { setDataToPresenter(forceUpdate = true) }

        with(profileCoursesRecycler) {
            adapter = coursesAdapter
            layoutManager = GridLayoutManager(context, ROW_COUNT, GridLayoutManager.HORIZONTAL, false)
            itemAnimator?.changeDuration = 0
            addItemDecoration(RightMarginForLastItems(resources.getDimensionPixelSize(R.dimen.home_right_recycler_padding_without_extra), ROW_COUNT))
            val snapHelper = CoursesSnapHelper(ROW_COUNT)
            snapHelper.attachToRecyclerView(this)
        }
    }

    private fun setDataToPresenter(forceUpdate: Boolean = false) {
        profileCoursesPresenter.fetchCourses(forceUpdate)
    }

    override fun onStart() {
        super.onStart()

        profileCoursesPresenter.attachView(this)
    }

    override fun onStop() {
        profileCoursesPresenter.detachView(this)
        super.onStop()
    }

    override fun setState(state: ProfileCoursesView.State) {
        viewStateDelegate.switchState(state)

        when (state) {
            is ProfileCoursesView.State.Content -> {
                profileCoursesCount.text = resources.getQuantityString(R.plurals.course_count, state.courseListDataItems.size, state.courseListDataItems.size)
                coursesAdapter.items = state.courseListDataItems
                (profileCoursesRecycler.layoutManager as? GridLayoutManager)
                    ?.spanCount = min(ROW_COUNT, state.courseListDataItems.size)
            }
        }
    }

    // TODO Functions from ProfileCoursesView will be replaced with CourseListView
    override fun setBlockingLoading(isLoading: Boolean) {
        if (isLoading) {
            ProgressHelper.activate(progressDialogFragment, activity?.supportFragmentManager, LoadingProgressDialogFragment.TAG)
        } else {
            ProgressHelper.dismiss(activity?.supportFragmentManager, LoadingProgressDialogFragment.TAG)
        }
    }

    override fun showCourse(course: Course, isAdaptive: Boolean) {
        if (isAdaptive) {
            screenManager.continueAdaptiveCourse(activity, course)
        } else {
            screenManager.showCourseModules(activity, course)
        }
    }

    override fun showSteps(course: Course, lastStep: LastStep) {
        screenManager.continueCourse(activity, course.id, lastStep)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        profileCoursesPresenter.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }
}