package org.stepik.android.view.user_reviews.ui.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.error_no_connection_with_button_small.*
import kotlinx.android.synthetic.main.fragment_user_reviews.*
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.ui.util.initCenteredToolbar
import org.stepic.droid.ui.util.snackbar
import org.stepik.android.domain.course.analytic.CourseViewSource
import org.stepik.android.domain.course_reviews.analytic.CourseReviewViewSource
import org.stepik.android.domain.course_reviews.analytic.CreateCourseReviewPressedAnalyticEvent
import org.stepik.android.domain.course_reviews.model.CourseReview
import org.stepik.android.domain.user_reviews.model.UserCourseReviewItem
import org.stepik.android.presentation.user_reviews.UserReviewsFeature
import org.stepik.android.presentation.user_reviews.UserReviewsViewModel
import org.stepik.android.view.course_reviews.ui.dialog.ComposeCourseReviewDialogFragment
import org.stepik.android.view.user_reviews.ui.adapter.decorator.UserCourseReviewItemDecoration
import org.stepik.android.view.user_reviews.ui.adapter.delegate.UserReviewsPlaceholderAdapterDelegate
import org.stepik.android.view.user_reviews.ui.adapter.delegate.UserReviewsPotentialAdapterDelegate
import org.stepik.android.view.user_reviews.ui.adapter.delegate.UserReviewsPotentialHeaderAdapterDelegate
import org.stepik.android.view.user_reviews.ui.adapter.delegate.UserReviewsReviewedAdapterDelegate
import org.stepik.android.view.user_reviews.ui.adapter.delegate.UserReviewsReviewedHeaderAdapterDelegate
import ru.nobird.android.presentation.redux.container.ReduxView
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter
import ru.nobird.android.view.base.ui.delegate.ViewStateDelegate
import ru.nobird.android.view.base.ui.extension.showIfNotExists
import ru.nobird.android.view.redux.ui.extension.reduxViewModel
import javax.inject.Inject

class UserReviewsFragment : Fragment(R.layout.fragment_user_reviews), ReduxView<UserReviewsFeature.State, UserReviewsFeature.Action.ViewAction> {

    companion object {
        fun newInstance(): Fragment =
            UserReviewsFragment()
    }

    @Inject
    internal lateinit var analytic: Analytic

    @Inject
    internal lateinit var screenManager: ScreenManager

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private val userReviewsViewModel: UserReviewsViewModel by reduxViewModel(this) { viewModelFactory }

    private val viewStateDelegate = ViewStateDelegate<UserReviewsFeature.State>()

    private val userReviewItemAdapter: DefaultDelegateAdapter<UserCourseReviewItem> = DefaultDelegateAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectComponent()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initCenteredToolbar(R.string.user_review_title, true)
        initViewStateDelegate()
        userReviewItemAdapter += UserReviewsPlaceholderAdapterDelegate()
        userReviewItemAdapter += UserReviewsPotentialHeaderAdapterDelegate()
        userReviewItemAdapter += UserReviewsPotentialAdapterDelegate(
            onCourseTitleClicked = { course ->
                screenManager.showCourseDescription(requireContext(), course, CourseViewSource.UserReviews)
            },
            onWriteReviewClicked = { courseId, courseRating ->
                analytic.report(CreateCourseReviewPressedAnalyticEvent(courseId, CourseReviewViewSource.USER_REVIEWS_SOURCE))
                showCourseReviewEditDialog(courseId, courseReview = null, courseRating = courseRating)
            }
        )
        userReviewItemAdapter += UserReviewsReviewedHeaderAdapterDelegate()
        userReviewItemAdapter += UserReviewsReviewedAdapterDelegate(
            onCourseTitleClicked = { course ->
                screenManager.showCourseDescription(requireContext(), course, CourseViewSource.UserReviews)
            },
            onEditReviewClicked = { courseReview ->
                showCourseReviewEditDialog(courseReview.course, courseReview, -1f)
            },
            onRemoveReviewClicked = { courseReview -> userReviewsViewModel.onNewMessage(UserReviewsFeature.Message.DeletedReviewUserReviews(courseReview)) }
        )
        with(userReviewsRecycler) {
            adapter = userReviewItemAdapter
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            addItemDecoration(UserCourseReviewItemDecoration(
                separatorColor = ContextCompat.getColor(context, R.color.color_divider),
                UserCourseReviewItemDecoration.SeparatorSize(resources.getDimensionPixelSize(R.dimen.comment_item_separator_big))
            ))
        }
        userReviewsViewModel.onNewMessage(UserReviewsFeature.Message.InitMessage(forceUpdate = false))
        tryAgain.setOnClickListener {
            userReviewsViewModel.onNewMessage(UserReviewsFeature.Message.InitMessage(forceUpdate = true))
        }
    }

    private fun injectComponent() {
        App.componentManager()
            .learningActionsComponent()
            .inject(this)
    }

    private fun initViewStateDelegate() {
        viewStateDelegate.addState<UserReviewsFeature.State.Idle>()
        viewStateDelegate.addState<UserReviewsFeature.State.Loading>(userReviewsRecycler)
        viewStateDelegate.addState<UserReviewsFeature.State.Error>(userReviewsError)
        viewStateDelegate.addState<UserReviewsFeature.State.Empty>(userReviewsEmpty)
        viewStateDelegate.addState<UserReviewsFeature.State.Content>(userReviewsRecycler)
    }

    override fun onAction(action: UserReviewsFeature.Action.ViewAction) {
        when (action) {
            is UserReviewsFeature.Action.ViewAction.ShowDeleteSuccessSnackbar ->
                view?.snackbar(R.string.user_review_delete_success)
            is UserReviewsFeature.Action.ViewAction.ShowDeleteFailureSnackbar ->
                view?.snackbar(R.string.user_review_delete_failure)
        }
    }

    override fun render(state: UserReviewsFeature.State) {
        viewStateDelegate.switchState(state)
        if (state is UserReviewsFeature.State.Loading) {
            userReviewItemAdapter.items = listOf(
                UserCourseReviewItem.Placeholder(),
                UserCourseReviewItem.Placeholder(),
                UserCourseReviewItem.Placeholder(),
                UserCourseReviewItem.Placeholder(),
                UserCourseReviewItem.Placeholder(),
                UserCourseReviewItem.Placeholder()
            )
        }
        if (state is UserReviewsFeature.State.Content) {
            userReviewItemAdapter.items = state.userCourseReviewsResult.userCourseReviewItems
        }
    }

    private fun showCourseReviewEditDialog(courseId: Long, courseReview: CourseReview?, courseRating: Float) {
        val supportFragmentManager = activity
            ?.supportFragmentManager
            ?: return

        val requestCode =
            if (courseReview == null) {
                ComposeCourseReviewDialogFragment.CREATE_REVIEW_REQUEST_CODE
            } else {
                ComposeCourseReviewDialogFragment.EDIT_REVIEW_REQUEST_CODE
            }

        val dialog = ComposeCourseReviewDialogFragment.newInstance(courseId, CourseReviewViewSource.USER_REVIEWS_SOURCE, courseReview, courseRating)
        dialog.setTargetFragment(this, requestCode)
        dialog.showIfNotExists(supportFragmentManager, ComposeCourseReviewDialogFragment.TAG)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            ComposeCourseReviewDialogFragment.CREATE_REVIEW_REQUEST_CODE ->
                data?.takeIf { resultCode == Activity.RESULT_OK }
                    ?.getParcelableExtra<CourseReview>(ComposeCourseReviewDialogFragment.ARG_COURSE_REVIEW)
                    ?.let { userReviewsViewModel.onNewMessage(UserReviewsFeature.Message.NewReviewSubmission(it)) }

            ComposeCourseReviewDialogFragment.EDIT_REVIEW_REQUEST_CODE ->
                data?.takeIf { resultCode == Activity.RESULT_OK }
                    ?.getParcelableExtra<CourseReview>(ComposeCourseReviewDialogFragment.ARG_COURSE_REVIEW)
                    ?.let { userReviewsViewModel.onNewMessage(UserReviewsFeature.Message.EditReviewSubmission(it)) }

            else ->
                super.onActivityResult(requestCode, resultCode, data)
        }
    }
}