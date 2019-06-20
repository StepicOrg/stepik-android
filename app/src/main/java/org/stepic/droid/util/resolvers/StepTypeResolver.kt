package org.stepic.droid.util.resolvers

import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import org.stepic.droid.base.StepBaseFragment
import org.stepik.android.model.Step
import org.stepic.droid.ui.quiz.QuizDelegate

interface StepTypeResolver {

    @DrawableRes
    fun getDrawableForType(type: String?, isPeerReview: Boolean): Int

    @ColorRes
    fun getDrawableTintForStep(isViewed: Boolean): Int

    fun getFragment(step: Step?): StepBaseFragment
    fun getQuizDelegate(step: Step?): QuizDelegate

    fun isNeedUseOldStepContainer(step: Step): Boolean
}
