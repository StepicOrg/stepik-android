package org.stepik.android.view.step_quiz.ui.factory

import android.view.View
import androidx.annotation.LayoutRes
import org.stepik.android.view.step_quiz.ui.delegate.StepQuizFormDelegate

interface StepQuizFormFactory {
    @LayoutRes
    fun getLayoutResForStep(blockName: String?): Int

    fun getDelegateForStep(blockName: String?, view: View): StepQuizFormDelegate?
}