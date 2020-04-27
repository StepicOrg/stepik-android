package org.stepik.android.view.step_quiz_unsupported.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_step_quiz_unsupported.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.persistence.model.StepPersistentWrapper
import ru.nobird.android.view.base.ui.extension.argument
import javax.inject.Inject

class UnsupportedStepQuizFragment : Fragment() {
    companion object {
        fun newInstance(stepId: Long): UnsupportedStepQuizFragment =
            UnsupportedStepQuizFragment()
                .apply {
                    this.stepId = stepId
                }
    }

    @Inject
    internal lateinit var screenManager: ScreenManager

    @Inject
    internal lateinit var stepWrapper: StepPersistentWrapper

    private var stepId: Long by argument()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectComponent()
    }

    private fun injectComponent() {
        App.componentManager()
            .stepComponent(stepId)
            .inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_step_quiz_unsupported, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        stepQuizAction.setOnClickListener { screenManager.openStepInWeb(context, stepWrapper.step) }
    }
}