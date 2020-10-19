package org.stepic.droid.util.resolvers

import org.stepic.droid.ui.quiz.QuizDelegate
import org.stepik.android.model.Step

interface StepTypeResolver {

    fun getDrawableForType(type: String?, isPeerReview: Boolean): Pair<Int, Int>

    fun getQuizDelegate(step: Step?): QuizDelegate
}
