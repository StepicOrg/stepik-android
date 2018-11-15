package org.stepic.droid.ui.custom

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.stepic.droid.R
import org.stepik.android.model.Step
import javax.inject.Inject

class StepTextWrapper
@Inject
constructor() {
    private var latexLayout: LatexSupportableEnhancedFrameLayout? = null

    private var stepText: String? = null

    fun attach(parent: ViewGroup, attachToTop: Boolean = true) {
        if (latexLayout == null) {
            latexLayout = LayoutInflater.from(parent.context).inflate(R.layout.step_text_header, parent, false)
                    as LatexSupportableEnhancedFrameLayout
        }

        if (attachToTop) {
            parent.addView(latexLayout, 0)
        } else {
            parent.addView(latexLayout)
        }
    }

    fun bind(step: Step?) {
        val text = step?.block?.text?.takeIf(String::isNotEmpty)
        if (text == stepText) return
        stepText = text

        val layout = latexLayout ?: return

        if (text != null) {
            layout.setText(text)
            layout.setTextIsSelectable(true)
            layout.visibility = View.VISIBLE
        } else {
            layout.visibility = View.GONE
        }
    }

    fun detach(parent: ViewGroup) {
        latexLayout?.let(parent::removeView)
    }
}