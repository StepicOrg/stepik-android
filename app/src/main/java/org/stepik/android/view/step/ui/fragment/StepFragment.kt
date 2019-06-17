package org.stepik.android.view.step.ui.fragment

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.fragment_step.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepic.droid.ui.util.changeVisibility
import org.stepic.droid.util.argument
import org.stepik.android.domain.lesson.model.LessonData
import org.stepik.android.domain.step.model.StepNavigationDirection
import org.stepik.android.presentation.step.StepPresenter
import org.stepik.android.presentation.step.StepView
import org.stepik.android.view.step.ui.delegate.StepDiscussionsDelegate
import org.stepik.android.view.step.ui.delegate.StepNavigationDelegate
import org.stepik.android.view.step_content.ui.factory.StepContentFragmentFactory
import javax.inject.Inject

class StepFragment : Fragment(), StepView {
    companion object {
        private const val STEP_CONTENT_FRAGMENT_TAG = "step_content"

        fun newInstance(stepWrapper: StepPersistentWrapper, lessonData: LessonData): Fragment =
            StepFragment()
                .apply {
                    this.stepWrapper = stepWrapper
                    this.lessonData = lessonData
                }
    }

    @Inject
    internal lateinit var screenManager: ScreenManager

    @Inject
    internal lateinit var stepContentFragmentFactory: StepContentFragmentFactory

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var stepPresenter: StepPresenter

    private var stepWrapper: StepPersistentWrapper by argument()
    private var lessonData: LessonData by argument()

    private lateinit var stepNavigationDelegate: StepNavigationDelegate
    private lateinit var stepDiscussionsDelegate: StepDiscussionsDelegate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectComponent()

        stepPresenter = ViewModelProviders.of(this, viewModelFactory).get(StepPresenter::class.java)
        stepPresenter.onLessonData(stepWrapper, lessonData)
    }

    private fun injectComponent() {
        App.component()
            .stepComponentBuilder()
            .build()
            .inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_step, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        stepNavigationDelegate = StepNavigationDelegate(stepNavigation, stepPresenter::onStepDirectionClicked)

        stepDiscussionsDelegate = StepDiscussionsDelegate(stepDiscussions)
        stepDiscussionsDelegate.setDiscussionsCount(100)

        initStepContentFragment()
    }

    private fun initStepContentFragment() {
        stepContentContainer.layoutParams = (stepContentContainer.layoutParams as LinearLayoutCompat.LayoutParams)
            .apply {
                if (stepContentFragmentFactory.isStepCanHaveQuiz(stepWrapper)) {
                    height = LinearLayout.LayoutParams.WRAP_CONTENT
                    weight = 0f
                } else {
                    height = 0
                    weight = 1f
                }
            }
        stepQuizContainer.changeVisibility(stepContentFragmentFactory.isStepCanHaveQuiz(stepWrapper))

        if (childFragmentManager.findFragmentByTag(STEP_CONTENT_FRAGMENT_TAG) == null) {
            childFragmentManager
                .beginTransaction()
                .add(R.id.stepContentContainer, stepContentFragmentFactory.createStepContentFragment(stepWrapper))
                .commitNow()
        }
    }

    override fun onStart() {
        super.onStart()
        stepPresenter.attachView(this)
    }

    override fun onStop() {
        stepPresenter.detachView(this)
        super.onStop()
    }

    override fun setNavigation(directions: Set<StepNavigationDirection>) {
        stepNavigationDelegate.setState(directions)
    }

    override fun showLesson(direction: StepNavigationDirection, lessonData: LessonData) {
        val unit = lessonData.unit ?: return
        val section = lessonData.section ?: return

        screenManager.showSteps(activity, unit, lessonData.lesson, direction == StepNavigationDirection.PREV, section)
    }
}