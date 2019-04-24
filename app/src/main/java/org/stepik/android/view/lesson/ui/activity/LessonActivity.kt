package org.stepik.android.view.lesson.ui.activity

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.MenuItem
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.base.FragmentActivityBase
import org.stepik.android.presentation.lesson.LessonPresenter
import org.stepik.android.presentation.lesson.LessonView
import javax.inject.Inject

class LessonActivity : FragmentActivityBase(), LessonView {
    companion object {

    }

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var lessonPresenter: LessonPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lesson)

        injectComponent()
        lessonPresenter = ViewModelProviders
            .of(this, viewModelFactory)
            .get(LessonPresenter::class.java)
    }

    private fun injectComponent() {
        App.component()
            .lessonComponentBuilder()
            .build()
            .inject(this)
    }

    override fun onStart() {
        super.onStart()
        lessonPresenter.attachView(this)
    }

    override fun onStop() {
        lessonPresenter.detachView(this)
        super.onStop()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
             else ->
                super.onOptionsItemSelected(item)
        }

    override fun setState(state: LessonView.State) {

    }
}