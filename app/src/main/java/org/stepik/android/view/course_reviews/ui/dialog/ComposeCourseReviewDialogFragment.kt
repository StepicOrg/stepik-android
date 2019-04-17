package org.stepik.android.view.course_reviews.ui.dialog

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.DialogFragment
import org.stepic.droid.base.App
import org.stepik.android.domain.course_reviews.model.CourseReview
import org.stepik.android.presentation.course_reviews.ComposeCourseReviewPresenter
import javax.inject.Inject

class ComposeCourseReviewDialogFragment : DialogFragment() {
    companion object {
        private const val ARG_COURSE_REVIEW = "course_review"

        fun newInstance(courseReview: CourseReview?): DialogFragment =
            ComposeCourseReviewDialogFragment().apply {
                arguments = Bundle(1)
                    .also {
                        it.putParcelable(ARG_COURSE_REVIEW, courseReview)
                    }
            }
    }

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var composeCourseReviewPresenter: ComposeCourseReviewPresenter

    private val courseReview: CourseReview? by lazy { arguments?.getParcelable<CourseReview>(ARG_COURSE_REVIEW) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectComponent()

        composeCourseReviewPresenter = ViewModelProviders
            .of(this, viewModelFactory)
            .get(ComposeCourseReviewPresenter::class.java)
    }

    private fun injectComponent() {
        App.component()
            .composeCourseReviewComponent()
            .build()
            .inject(this)
    }
}