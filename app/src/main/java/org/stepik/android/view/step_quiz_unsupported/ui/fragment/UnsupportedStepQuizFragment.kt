package org.stepik.android.view.step_quiz_unsupported.ui.fragment

import android.os.Bundle
import androidx.core.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_step_quiz_unsupported.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepic.droid.ui.util.setCompoundDrawables
import org.stepic.droid.util.argument
import javax.inject.Inject

class UnsupportedStepQuizFragment : Fragment() {
    companion object {
        fun newInstance(stepWrapper: StepPersistentWrapper): UnsupportedStepQuizFragment =
            UnsupportedStepQuizFragment()
                .apply {
                    this.stepWrapper = stepWrapper
                }
    }

    @Inject
    internal lateinit var screenManager: ScreenManager

    private var stepWrapper: StepPersistentWrapper by argument()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectComponent()
    }

    private fun injectComponent() {
        App.component()
            .stepComponentBuilder()
            .build()
            .inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_step_quiz_unsupported, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        stepQuizAction.setOnClickListener { screenManager.openStepInWeb(context, stepWrapper.step) }
        stepQuizFeedback.setCompoundDrawables(start = R.drawable.ic_step_quiz_validation)
    }
}