package org.stepik.android.view.step_quz_pycharm.ui.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_step_quiz_pycharm.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepic.droid.ui.util.setCompoundDrawables
import org.stepic.droid.util.argument
import javax.inject.Inject

class PyCharmStepQuizFragment : Fragment() {
    companion object {
        fun newInstance(stepWrapper: StepPersistentWrapper): PyCharmStepQuizFragment =
            PyCharmStepQuizFragment()
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
        inflater.inflate(R.layout.fragment_step_quiz_pycharm, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        stepQuizFeedback.movementMethod = LinkMovementMethod.getInstance()
        stepQuizFeedback.setCompoundDrawables(start = R.drawable.ic_step_quiz_validation)
    }
}