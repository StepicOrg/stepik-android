package org.stepik.android.view.step_quiz_fullscreen_code.ui.adapter

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import org.stepic.droid.R
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepik.android.domain.lesson.model.LessonData
import org.stepik.android.view.step_quiz_fullscreen_code.ui.fragment.CodeStepQuizFullScreenInstructionFragment
import org.stepik.android.view.step_quiz_fullscreen_code.ui.fragment.CodeStepQuizFullScreenPlaygroundFragment

class CodeStepQuizFullScreenAdapter(
    context: Context,
    lang: String,
    fragmentManager: FragmentManager,
    stepPersistentWrapper: StepPersistentWrapper,
    lessonData: LessonData
) : FragmentPagerAdapter(fragmentManager) {
    private val fragments = listOf(
        { CodeStepQuizFullScreenInstructionFragment.newInstance(stepPersistentWrapper, lang) }
                to context.resources.getString(R.string.step_quiz_code_full_screen_instruction_tab),
        { CodeStepQuizFullScreenPlaygroundFragment.newInstance(stepPersistentWrapper, lessonData) }
                to context.resources.getString(R.string.step_quiz_code_full_screen_code_tab)
    )

    override fun getItem(position: Int): Fragment =
        fragments[position].first.invoke()

    override fun getCount(): Int =
        fragments.size

    override fun getPageTitle(position: Int): CharSequence =
        fragments[position].second
}