package org.stepik.android.view.step_content_text.ui.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.stepic.droid.R
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepic.droid.ui.custom.LatexSupportableEnhancedFrameLayout
import org.stepic.droid.ui.util.changeVisibility
import org.stepic.droid.util.argument

class TextStepContentFragment : Fragment() {
    companion object {
        fun newInstance(stepPersistentWrapper: StepPersistentWrapper): Fragment =
            TextStepContentFragment()
                .apply {
                    this.stepWrapper = stepPersistentWrapper
                }
    }

    private var stepWrapper: StepPersistentWrapper by argument()
    private var latexLayout: LatexSupportableEnhancedFrameLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        latexLayout ?: inflater.inflate(R.layout.step_text_header, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (latexLayout == null) {
            latexLayout = view as LatexSupportableEnhancedFrameLayout

            val text = stepWrapper
                .step
                .block
                ?.text
                ?.takeIf(String::isNotEmpty)

            view.changeVisibility(needShow = text != null)
            view.setTextSize(16f)
            if (text != null) {
                view.setText(text)
                view.setTextIsSelectable(true)
            }
        }
    }

    override fun onDestroy() {
        latexLayout = null
        super.onDestroy()
    }
}