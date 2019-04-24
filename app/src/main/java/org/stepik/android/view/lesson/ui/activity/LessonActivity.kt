package org.stepik.android.view.lesson.ui.activity

import android.os.Bundle
import org.stepic.droid.R
import org.stepic.droid.base.FragmentActivityBase
import org.stepik.android.presentation.lesson.LessonView

class LessonActivity : FragmentActivityBase(), LessonView {
    companion object {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lesson)
    }

    override fun setState(state: LessonView.State) {

    }
}