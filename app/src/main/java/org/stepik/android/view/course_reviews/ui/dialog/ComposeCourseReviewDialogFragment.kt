package org.stepik.android.view.course_reviews.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.dialog_compose_course_review.*
import kotlinx.android.synthetic.main.view_centered_toolbar.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.ui.dialogs.LoadingProgressDialogFragment
import org.stepic.droid.ui.util.snackbar
import org.stepic.droid.util.ProgressHelper
import org.stepik.android.domain.course_reviews.model.CourseReview
import org.stepik.android.presentation.course_reviews.ComposeCourseReviewPresenter
import org.stepik.android.presentation.course_reviews.ComposeCourseReviewView
import org.stepik.android.view.base.ui.extension.setTintList
import ru.nobird.android.view.base.ui.extension.argument
import ru.nobird.android.view.base.ui.extension.hideKeyboard
import javax.inject.Inject

class ComposeCourseReviewDialogFragment : DialogFragment(), ComposeCourseReviewView {
    companion object {
        const val TAG = "ComposeCourseReviewDialogFragment"
        const val CREATE_REVIEW_REQUEST_CODE = 3412
        const val EDIT_REVIEW_REQUEST_CODE = CREATE_REVIEW_REQUEST_CODE + 1

        const val ARG_COURSE_REVIEW = "course_review"

        fun newInstance(courseId: Long, courseReview: CourseReview?): DialogFragment =
            ComposeCourseReviewDialogFragment().apply {
                this.arguments = Bundle(2)
                    .also {
                        it.putParcelable(ARG_COURSE_REVIEW, courseReview)
                    }
                this.courseId = courseId
            }
    }

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var composeCourseReviewPresenter: ComposeCourseReviewPresenter

    private var courseId: Long by argument()
    private val courseReview: CourseReview? by lazy { arguments?.getParcelable<CourseReview>(ARG_COURSE_REVIEW) }

    private val progressDialogFragment: DialogFragment =
        LoadingProgressDialogFragment.newInstance()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        return dialog
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.ThemeOverlay_AppTheme_Dialog_Fullscreen)

        injectComponent()
        composeCourseReviewPresenter = ViewModelProviders
            .of(this, viewModelFactory)
            .get(ComposeCourseReviewPresenter::class.java)
    }

    private fun injectComponent() {
        App.component()
            .composeCourseReviewComponentBuilder()
            .build()
            .inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.dialog_compose_course_review, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        centeredToolbarTitle.setText(R.string.course_reviews_compose_title)
        centeredToolbar.setNavigationOnClickListener { dismiss() }
        centeredToolbar.navigationIcon = AppCompatResources
            .getDrawable(requireContext(), R.drawable.ic_close_dark)
            ?.setTintList(requireContext(), R.attr.colorControlNormal)
        centeredToolbar.inflateMenu(R.menu.compose_course_review_menu)
        centeredToolbar.setOnMenuItemClickListener { menuItem ->
            if (menuItem.itemId == R.id.course_review_submit) {
                submitCourseReview()
                true
            } else {
                false
            }
        }

        if (savedInstanceState == null) {
            courseReview?.let {
                courseReviewEditText.setText(it.text)
                courseReviewRating.rating = it.score.toFloat()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog
            ?.window
            ?.let { window ->
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,  ViewGroup.LayoutParams.MATCH_PARENT)
                window.setWindowAnimations(R.style.ThemeOverlay_AppTheme_Dialog_Fullscreen)
            }

        composeCourseReviewPresenter.attachView(this)
    }

    override fun onStop() {
        composeCourseReviewPresenter.detachView(this)
        super.onStop()
    }

    private fun submitCourseReview() {
        courseReviewEditText.hideKeyboard()
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
        when (state) {
            ComposeCourseReviewView.State.Idle ->
                ProgressHelper.dismiss(childFragmentManager, LoadingProgressDialogFragment.TAG)

            ComposeCourseReviewView.State.Loading ->
                ProgressHelper.activate(progressDialogFragment, childFragmentManager, LoadingProgressDialogFragment.TAG)

            is ComposeCourseReviewView.State.Complete -> {
                ProgressHelper.dismiss(childFragmentManager, LoadingProgressDialogFragment.TAG)
                targetFragment
                    ?.onActivityResult(
                        targetRequestCode,
                        Activity.RESULT_OK,
                        Intent().putExtra(ARG_COURSE_REVIEW, state.courseReview)
                    )
                dismiss()
            }
        }
    }

    override fun showNetworkError() {
        view?.snackbar(messageRes = R.string.connectionProblems)
    }
}