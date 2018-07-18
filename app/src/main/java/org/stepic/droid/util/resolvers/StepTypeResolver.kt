package org.stepic.droid.util.resolvers

import android.graphics.drawable.Drawable
import org.stepic.droid.base.StepBaseFragment
import org.stepik.android.model.structure.Step
import org.stepic.droid.ui.quiz.QuizDelegate

interface StepTypeResolver {

    fun getDrawableForType(type: String?, viewed: Boolean, isPeerReview: Boolean): Drawable

    fun getFragment(step: Step?): StepBaseFragment
    fun getQuizDelegate(step: Step?): QuizDelegate
}
