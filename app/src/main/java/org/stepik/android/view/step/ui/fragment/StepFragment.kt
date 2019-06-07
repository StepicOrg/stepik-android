package org.stepik.android.view.step.ui.fragment

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_step.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepic.droid.util.argument
import org.stepik.android.domain.lesson.model.LessonData
import org.stepik.android.presentation.step.StepPresenter
import org.stepik.android.presentation.step.StepView
import org.stepik.android.view.step.ui.delegate.StepNavigationDelegate
import javax.inject.Inject

class StepFragment : Fragment(), StepView {
    companion object {
        fun newInstance(stepWrapper: StepPersistentWrapper, lessonData: LessonData): Fragment =
            StepFragment()
                .apply {
                    this.stepWrapper = stepWrapper
                    this.lessonData = lessonData
                }
    }

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var stepPresenter: StepPresenter

    private var stepWrapper: StepPersistentWrapper by argument()
    private var lessonData: LessonData by argument()

    private lateinit var stepNavigationDelegate: StepNavigationDelegate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectComponent()

        stepPresenter = ViewModelProviders.of(this, viewModelFactory).get(StepPresenter::class.java)
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
        stepNavigationDelegate = StepNavigationDelegate(stepNavigation)
        stepNavigationDelegate.setState(true, true)
    }

    override fun onStart() {
        super.onStart()
        stepPresenter.attachView(this)
    }

    override fun onStop() {
        stepPresenter.detachView(this)
        super.onStop()
    }
}