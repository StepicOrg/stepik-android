package org.stepik.android.view.step_quiz_matching.ui.fragment

import androidx.core.app.Fragment
import android.view.View
import kotlinx.android.synthetic.main.layout_step_quiz_sorting.*
import org.stepic.droid.R
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepik.android.domain.lesson.model.LessonData
import org.stepik.android.view.step_quiz.ui.delegate.StepQuizFormDelegate
import org.stepik.android.view.step_quiz.ui.fragment.DefaultStepQuizFragment
import org.stepik.android.view.step_quiz_matching.ui.delegate.MatchingStepQuizFormDelegate

class MatchingStepQuizFragment : DefaultStepQuizFragment() {
    companion object {
        fun newInstance(stepPersistentWrapper: StepPersistentWrapper, lessonData: LessonData): Fragment =
            MatchingStepQuizFragment()
                .apply {
                    this.stepWrapper = stepPersistentWrapper
                    this.lessonData = lessonData
                }
    }

    override val quizLayoutRes: Int =
        R.layout.layout_step_quiz_sorting

    override val quizViews: Array<View>
        get() = arrayOf(sortingRecycler)

    override fun createStepQuizFormDelegate(view: View): StepQuizFormDelegate =
        MatchingStepQuizFormDelegate(view)
}