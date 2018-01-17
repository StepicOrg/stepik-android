package org.stepic.droid.adaptive.ui.fragments

import android.os.Bundle
import org.stepic.droid.base.App
import org.stepic.droid.base.FragmentBase
import org.stepic.droid.core.presenters.RecommendationsPresenter
import javax.inject.Inject

class RecommendationsFragment : FragmentBase() {
    companion object {
        private const val COURSE_ID_KEY = "course_id"

        fun newInstance(courseId: Long): RecommendationsFragment {
            val args = Bundle().apply { putLong(COURSE_ID_KEY, courseId) }
            return RecommendationsFragment().apply { arguments = args }
        }
    }

    @Inject
    lateinit var recommendationsPresenter: RecommendationsPresenter

    private var courseId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        courseId = arguments.getLong(COURSE_ID_KEY)
        super.onCreate(savedInstanceState)
    }

    override fun injectComponent() {
        App.componentManager()
                .adaptiveCourseComponent(courseId)
                .inject(this)
    }

    override fun onReleaseComponent() {
        App.componentManager()
                .releaseAdaptiveCourseComponent(courseId)
    }

    fun onCourseNotSupported() {
        // show error if course is not adaptive or not in list of accepted courses
    }
}