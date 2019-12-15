package org.stepik.android.view.profile_courses.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ru.nobird.android.view.base.ui.extension.argument
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.error_no_connection_with_button_small.*
import kotlinx.android.synthetic.main.fragment_profile_courses.*
import org.stepic.droid.R
import org.stepic.droid.adaptive.util.AdaptiveCoursesResolver
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.ui.decorators.RightMarginForLastItems
import org.stepic.droid.ui.util.CoursesSnapHelper
import org.stepik.android.domain.last_step.model.LastStep
import org.stepik.android.model.Course
import org.stepik.android.presentation.profile_courses.ProfileCoursesPresenter
import org.stepik.android.presentation.profile_courses.ProfileCoursesView
import org.stepik.android.view.course_list.ui.adapter.delegate.CourseAdapterDelegate
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter
import javax.inject.Inject

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
    internal lateinit var adaptiveCoursesResolver: AdaptiveCoursesResolver

    @Inject
    internal lateinit var analytic: Analytic

    @Inject
    internal lateinit var screenManager: ScreenManager

    private var userId by argument<Long>()

    private lateinit var profileCoursesPresenter: ProfileCoursesPresenter

    private lateinit var coursesAdapter: DefaultDelegateAdapter<Course>
    private lateinit var viewStateDelegate: ViewStateDelegate<ProfileCoursesView.State>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        injectComponent()

        profileCoursesPresenter = ViewModelProviders
            .of(this, viewModelFactory)
            .get(ProfileCoursesPresenter::class.java)

        coursesAdapter = DefaultDelegateAdapter()
        coursesAdapter += CourseAdapterDelegate(
            adaptiveCoursesResolver,
            onItemClicked = ::onCourseClicked,
            onContinueCourseClicked = profileCoursesPresenter::continueCourse
        )
    }

    private fun injectComponent() {
        App.componentManager()
            .profileComponent(userId)
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
                profileCoursesCount.text = resources.getQuantityString(R.plurals.course_count, state.courses.size, state.courses.size)
                coursesAdapter.items = state.courses
            }
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

    private fun onCourseClicked(course: Course) {
        analytic.reportEvent(Analytic.Interaction.CLICK_COURSE)
        if (course.enrollment != 0L) {
            if (adaptiveCoursesResolver.isAdaptive(course.id)) {
                screenManager.continueAdaptiveCourse(activity, course)
            } else {
                screenManager.showCourseModules(activity, course)
            }
        } else {
            screenManager.showCourseDescription(activity, course)
        }
    }
}