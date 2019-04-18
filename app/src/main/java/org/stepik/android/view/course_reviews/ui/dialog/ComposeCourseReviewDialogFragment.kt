package org.stepik.android.view.course_reviews.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.DialogFragment
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import kotlinx.android.synthetic.main.dialog_compose_course_review.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.util.argument
import org.stepic.droid.util.setTextColor
import org.stepik.android.domain.course_reviews.model.CourseReview
import org.stepik.android.presentation.course_reviews.ComposeCourseReviewPresenter
import org.stepik.android.presentation.course_reviews.ComposeCourseReviewView
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import javax.inject.Inject

class ComposeCourseReviewDialogFragment : DialogFragment(), ComposeCourseReviewView {
    companion object {
        const val TAG = "ComposeCourseReviewDialogFragment"
        const val REQUEST_CODE = 3412

        const val ARG_COURSE_REVIEW = "course_review"

        fun newInstance(courseId: Long, courseReview: CourseReview?): DialogFragment =
            ComposeCourseReviewDialogFragment().apply {
                this.courseId = courseId
                this.arguments = Bundle(1)
                    .also {
                        it.putParcelable(ARG_COURSE_REVIEW, courseReview)
                    }
            }
    }

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var composeCourseReviewPresenter: ComposeCourseReviewPresenter
    private lateinit var viewStateDelegate: ViewStateDelegate<ComposeCourseReviewView.State>

    private var courseId: Long by argument()
    private val courseReview: CourseReview? by lazy { arguments?.getParcelable<CourseReview>(ARG_COURSE_REVIEW) }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        return dialog
    }

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.dialog_compose_course_review, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewStateDelegate = ViewStateDelegate()
        viewStateDelegate.addState<ComposeCourseReviewView.State.Idle>(courseReviewIdle)
        viewStateDelegate.addState<ComposeCourseReviewView.State.Loading>(courseReviewLoading)
        viewStateDelegate.addState<ComposeCourseReviewView.State.Complete>(courseReviewIdle)

        if (savedInstanceState == null) {
            courseReview?.let {
                courseReviewEditText.setText(it.text)
                courseReviewRating.rating = it.score.toFloat()
            }
        }
    }
    override fun onStart() {
        super.onStart()
        composeCourseReviewPresenter.attachView(this)
    }

    override fun onStop() {
        composeCourseReviewPresenter.detachView(this)
        super.onStop()
    }
    
    private fun submitCourseReview() {
        val oldCourseReview = courseReview

        val text = courseReviewEditText.text?.toString()
        val score = courseReviewRating.rating.toInt()

        if (oldCourseReview == null) {
            val courseReview = CourseReview(
                course = courseId,
                text = text,
                score = score
            )
            composeCourseReviewPresenter.createCourseReview(courseReview)
        } else {
            val courseReview = oldCourseReview
                .copy(
                    text = text,
                    score = score
                )
            composeCourseReviewPresenter.updateCourseReview(oldCourseReview, courseReview)
        }
    }

    override fun setState(state: ComposeCourseReviewView.State) {
        viewStateDelegate.switchState(state)

        if (state is ComposeCourseReviewView.State.Complete) {
            targetFragment
                ?.onActivityResult(
                    REQUEST_CODE,
                    Activity.RESULT_OK,
                    Intent().putExtra(ARG_COURSE_REVIEW, state.courseReview)
                )
            dismiss()
        }
    }

    override fun showNetworkError() {
        val view = view
            ?: return

        Snackbar
            .make(view, R.string.connectionProblems, Snackbar.LENGTH_SHORT)
            .setTextColor(ContextCompat.getColor(view.context, R.color.white))
            .show()
    }
}