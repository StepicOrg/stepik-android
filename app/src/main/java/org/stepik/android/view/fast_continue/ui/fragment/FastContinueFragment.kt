package org.stepik.android.view.fast_continue.ui.fragment

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.BitmapImageViewTarget
import kotlinx.android.synthetic.main.fragment_fast_continue.*
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.ui.activities.MainFeedActivity
import org.stepic.droid.ui.dialogs.LoadingProgressDialogFragment
import org.stepic.droid.ui.util.RoundedBitmapImageViewTarget
import org.stepic.droid.util.ProgressHelper
import org.stepik.android.domain.course_list.model.CourseListItem
import org.stepik.android.domain.last_step.model.LastStep
import org.stepik.android.model.Course
import org.stepik.android.presentation.course_continue.model.CourseContinueInteractionSource
import org.stepik.android.presentation.fast_continue.FastContinuePresenter
import org.stepik.android.presentation.fast_continue.FastContinueView
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import javax.inject.Inject

class FastContinueFragment : Fragment(), FastContinueView {

    companion object {
        fun newInstance(): FastContinueFragment =
            FastContinueFragment()

        private const val CONTINUE_LOADING_TAG = "CONTINUE_LOADING_TAG"
    }

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    internal lateinit var analytic: Analytic

    @Inject
    internal lateinit var screenManager: ScreenManager

    private lateinit var fastContinuePresenter: FastContinuePresenter
    private lateinit var viewStateDelegate: ViewStateDelegate<FastContinueView.State>

    private lateinit var courseCoverImageViewTarget: BitmapImageViewTarget

    private val progressDialogFragment: DialogFragment =
        LoadingProgressDialogFragment.newInstance()

    private val coursePlaceholderDrawable by lazy {
        val coursePlaceholderBitmap = BitmapFactory.decodeResource(resources, R.drawable.general_placeholder)
        val circularBitmapDrawable = RoundedBitmapDrawableFactory.create(resources, coursePlaceholderBitmap)
        circularBitmapDrawable.cornerRadius = resources.getDimension(R.dimen.course_image_radius)
        return@lazy circularBitmapDrawable
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        injectComponent()

        fastContinuePresenter = ViewModelProviders
            .of(this, viewModelFactory)
            .get(FastContinuePresenter::class.java)
    }

    private fun injectComponent() {
        App
            .component()
            .fastContinueComponentBuilder()
            .build()
            .inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_fast_continue, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewStateDelegate = ViewStateDelegate()
        viewStateDelegate.addState<FastContinueView.State.Idle>()
        viewStateDelegate.addState<FastContinueView.State.Loading>(fastContinueProgress)
        viewStateDelegate.addState<FastContinueView.State.Empty>(fastContinuePlaceholder)
        viewStateDelegate.addState<FastContinueView.State.Anonymous>(fastContinuePlaceholder)
        viewStateDelegate.addState<FastContinueView.State.Content>(fastContinueMask)

        courseCoverImageViewTarget = RoundedBitmapImageViewTarget(resources.getDimension(R.dimen.course_image_radius), fastContinueCourseCover)
        fastContinueOverlay.isEnabled = true
        fastContinueAction.isEnabled = true
    }

    override fun onStart() {
        super.onStart()
        fastContinuePresenter.attachView(this)
    }

    override fun onStop() {
        fastContinuePresenter.detachView(this)
        super.onStop()
    }

    override fun setState(state: FastContinueView.State) {
        viewStateDelegate.switchState(state)
        when (state) {
            is FastContinueView.State.Empty -> {
                analytic.reportEvent(Analytic.FastContinue.EMPTY_COURSES_SHOWN)
                showPlaceholder(R.string.placeholder_explore_courses) {
                    analytic.reportEvent(Analytic.FastContinue.EMPTY_COURSES_CLICK)
                    screenManager.showCatalog(context)
                }
            }
            is FastContinueView.State.Anonymous -> {
                analytic.reportEvent(Analytic.FastContinue.AUTH_SHOWN)
                showPlaceholder(R.string.placeholder_login) {
                    analytic.reportEvent(Analytic.FastContinue.AUTH_CLICK)
                    screenManager.showLaunchScreen(context, true, MainFeedActivity.HOME_INDEX)
                }
            }
            is FastContinueView.State.Content -> {
                analytic.reportEvent(Analytic.FastContinue.CONTINUE_SHOWN)
                setCourse(state.courseListItem)
                fastContinueOverlay.setOnClickListener { handleContinueCourseClick(state.courseListItem.course) }
                fastContinueAction.setOnClickListener { handleContinueCourseClick(state.courseListItem.course) }
            }
        }
    }

    private fun setCourse(courseListItem: CourseListItem.Data) {
        Glide
            .with(requireContext())
            .asBitmap()
            .load(courseListItem.course.cover)
            .placeholder(coursePlaceholderDrawable)
            .fitCenter()
            .into(courseCoverImageViewTarget)

        fastContinueCourseName.text = courseListItem.course.title

        val progress = courseListItem.courseStats.progress
        val needShow = if (progress != null && progress.cost > 0) {
            val score = progress
                .score
                ?.toFloatOrNull()
                ?.toLong()
                ?: 0L

            fastContinueCourseProgressText.text = getString(R.string.course_current_progress, score, progress.cost)
            fastContinueCourseProgress.progress = (score * 100 / progress.cost).toInt()
            true
        } else {
            fastContinueCourseProgress.progress = 0
            false
        }
        fastContinueCourseProgressText.isVisible = needShow
    }

    private fun showPlaceholder(@StringRes stringRes: Int, listener: (view: View) -> Unit) {
        fastContinuePlaceholder.setPlaceholderText(stringRes)
        fastContinuePlaceholder.setOnClickListener(listener)
    }

    private fun handleContinueCourseClick(course: Course) {
        analytic.reportEvent(Analytic.FastContinue.CONTINUE_CLICK)
        fastContinuePresenter.continueCourse(course, CourseContinueInteractionSource.HOME_WIDGET)
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

    override fun setBlockingLoading(isLoading: Boolean) {
        fastContinueOverlay.isEnabled = !isLoading
        fastContinueAction.isEnabled = !isLoading
        if (isLoading) {
            ProgressHelper.activate(progressDialogFragment, fragmentManager, LoadingProgressDialogFragment.TAG)
        } else {
            ProgressHelper.dismiss(fragmentManager, LoadingProgressDialogFragment.TAG)
        }
    }
}
