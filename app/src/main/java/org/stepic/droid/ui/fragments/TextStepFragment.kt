package org.stepic.droid.ui.fragments

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_text_step.*
import org.stepic.droid.R
import org.stepic.droid.base.StepBaseFragment

class TextStepFragment : StepBaseFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
            = inflater.inflate(R.layout.fragment_text_step, container, false)

    override fun attachStepTextWrapper() {
        stepTextWrapper.attach(stepContainer)
    }

    override fun detachStepTextWrapper() {
        stepTextWrapper.detach(stepContainer)
    }
}
