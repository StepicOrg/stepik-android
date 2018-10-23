package org.stepic.droid.ui.custom

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.stepic.droid.R
import org.stepik.android.model.Step
import javax.inject.Inject

class StepTextWrapper
@Inject
constructor(
        private val context: Context
) {
    private lateinit var latexLayout: LatexSupportableEnhancedFrameLayout

    private var stepText: String? = null

    fun attach(parent: ViewGroup, attachToTop: Boolean = true) {
        if (!this::latexLayout.isInitialized) {
            latexLayout = LayoutInflater.from(context).inflate(R.layout.step_text_header, parent, false)
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

        if (text != null) {
            latexLayout.setText(text)
            latexLayout.setTextIsSelectable(true)
            latexLayout.visibility = View.VISIBLE
        } else {
            latexLayout.visibility = View.GONE
        }
    }

    fun detach(parent: ViewGroup) {
        parent.removeView(latexLayout)
    }
}