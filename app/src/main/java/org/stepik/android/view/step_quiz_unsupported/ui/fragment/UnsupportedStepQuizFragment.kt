package org.stepik.android.view.step_quiz_unsupported.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.reactivex.subjects.BehaviorSubject
import kotlinx.android.synthetic.main.fragment_step_quiz_unsupported.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepik.android.view.magic_links.ui.dialog.MagicLinkDialogFragment
import org.stepik.android.view.step.routing.StepDeepLinkBuilder
import ru.nobird.android.view.base.ui.extension.argument
import ru.nobird.android.view.base.ui.extension.showIfNotExists
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
    internal lateinit var stepDeepLinkBuilder: StepDeepLinkBuilder

    @Inject
    internal lateinit var stepWrapperBehaviorSubject: BehaviorSubject<StepPersistentWrapper>

    private var stepId: Long by argument()
    private lateinit var stepWrapper: StepPersistentWrapper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectComponent()
        stepWrapper = stepWrapperBehaviorSubject.value ?: throw IllegalArgumentException("Cannot be null")
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

        stepQuizAction.setOnClickListener {
            MagicLinkDialogFragment
                .newInstance(stepDeepLinkBuilder.createStepLink(stepWrapper.step))
                .showIfNotExists(childFragmentManager, MagicLinkDialogFragment.TAG)
        }
    }
}