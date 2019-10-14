package org.stepic.droid.util.resolvers

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import org.stepic.droid.ui.quiz.QuizDelegate
import org.stepik.android.model.Step

interface StepTypeResolver {

    @DrawableRes
    fun getDrawableForType(type: String?, isPeerReview: Boolean): Int

    @ColorRes
    fun getDrawableTintForStep(isViewed: Boolean): Int

    fun getQuizDelegate(step: Step?): QuizDelegate
}
