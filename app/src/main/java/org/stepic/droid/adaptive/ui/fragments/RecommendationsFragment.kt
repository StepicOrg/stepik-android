package org.stepic.droid.adaptive.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import dev.androidbroadcast.vbpd.viewBinding
import org.stepic.droid.R
import org.stepic.droid.adaptive.ui.adapters.QuizCardsAdapter
import org.stepic.droid.adaptive.ui.animations.RecommendationsFragmentAnimations
import org.stepic.droid.adaptive.ui.dialogs.AdaptiveLevelDialogFragment
import org.stepic.droid.base.App
import org.stepic.droid.base.FragmentBase
import org.stepic.droid.core.presenters.RecommendationsPresenter
import org.stepic.droid.core.presenters.contracts.RecommendationsView
import org.stepic.droid.databinding.FragmentRecommendationsBinding
import org.stepic.droid.ui.util.PopupHelper
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.resolveColorAttribute
import org.stepik.android.model.Course
import ru.nobird.android.view.base.ui.extension.showIfNotExists
import javax.inject.Inject

class RecommendationsFragment : FragmentBase(), RecommendationsView {
    companion object {
        fun newInstance(course: Course): RecommendationsFragment {
            val args = Bundle().apply { putParcelable(AppConstants.KEY_COURSE_BUNDLE, course) }
            return RecommendationsFragment().apply { arguments = args }
        }

        private const val MAX_UNFORMATTED_EXP = 1e6
        private fun formatExp(exp: Long) = if (exp > MAX_UNFORMATTED_EXP) {
            "${exp / 1000}K"
        } else {
            exp.toString()
        }

        private const val LEVEL_DIALOG_TAG = "level_dialog"
    }

    @Inject
    lateinit var recommendationsPresenter: RecommendationsPresenter

    private lateinit var animations: RecommendationsFragmentAnimations

    private val recommendationsBinding: FragmentRecommendationsBinding by viewBinding(FragmentRecommendationsBinding::bind)

    private var course: Course? = null

    private val loadingPlaceholders by lazy { resources.getStringArray(R.array.recommendation_loading_placeholders) }

    private var expPopupWindow: PopupWindow? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        course = arguments?.getParcelable(AppConstants.KEY_COURSE_BUNDLE)
        super.onCreate(savedInstanceState)
    }

    override fun injectComponent() {
        App.componentManager()
            .adaptiveCourseComponent(course?.id ?: 0)
            .inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_recommendations, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = requireContext()

        animations = RecommendationsFragmentAnimations(recommendationsBinding.streakSuccessContainer.context)

        recommendationsBinding.errorNoConnectionWithButton.error.setBackgroundColor(0)

        recommendationsBinding.errorNoConnectionWithButton.tryAgain.setOnClickListener {
            recommendationsPresenter.retry()
        }

        (activity as? AppCompatActivity)?.let {
            it.setSupportActionBar(recommendationsBinding.toolbar)
            it.supportActionBar?.setDisplayHomeAsUpEnabled(true)
            it.supportActionBar?.setDisplayShowTitleEnabled(false)
        }

        recommendationsBinding.toolbar.setOnClickListener {
            screenManager.showAdaptiveStats(context, course?.id ?: 0)

            expPopupWindow?.let { popup ->
                if (popup.isShowing) {
                    popup.dismiss()
                }
            }
        }

        recommendationsBinding.streakSuccessContainer.nestedTextView = recommendationsBinding.streakSuccess
        recommendationsBinding.streakSuccessContainer.setGradientDrawableParams(recommendationsBinding.streakSuccessContainer.context.resolveColorAttribute(R.attr.colorPrimary), 0f)
    }

    override fun onAdapter(cardsAdapter: QuizCardsAdapter) {
        recommendationsBinding.cardsContainer.setAdapter(cardsAdapter)
    }

    override fun onLoading() {
        recommendationsBinding.progress.visibility = View.VISIBLE
        recommendationsBinding.errorNoConnectionWithButton.error.visibility = View.GONE
        recommendationsBinding.loadingPlaceholder.text = loadingPlaceholders.random()
    }

    override fun onCardLoaded() {
        recommendationsBinding.progress.visibility = View.GONE
        recommendationsBinding.cardsContainer.visibility = View.VISIBLE
    }

    private fun onError() {
        recommendationsBinding.cardsContainer.visibility = View.GONE
        recommendationsBinding.errorNoConnectionWithButton.error.visibility = View.VISIBLE
        recommendationsBinding.progress.visibility = View.GONE
    }

    override fun onConnectivityError() {
        recommendationsBinding.errorNoConnectionWithButton.errorMessage.setText(R.string.no_connection)
        onError()
    }

    override fun onRequestError() {
        recommendationsBinding.errorNoConnectionWithButton.errorMessage.setText(R.string.request_error)
        onError()
    }

    private fun onCourseState() {
        recommendationsBinding.cardsContainer.visibility = View.GONE
        recommendationsBinding.progress.visibility = View.GONE
        recommendationsBinding.courseState.visibility = View.VISIBLE
    }

    override fun onCourseCompleted() {
        recommendationsBinding.courseStateText.setText(R.string.adaptive_course_completed)
        onCourseState()
    }

    override fun onCourseNotSupported() {
        recommendationsBinding.courseStateText.setText(R.string.adaptive_course_not_supported)
        onCourseState()
    }

    override fun updateExp(
        exp: Long,
        currentLevelExp: Long,
        nextLevelExp: Long,

        level: Long
    ) {
        recommendationsBinding.expProgress.progress = (exp - currentLevelExp).toInt()
        recommendationsBinding.expProgress.max = (nextLevelExp - currentLevelExp).toInt()

        recommendationsBinding.expCounter.text = formatExp(exp)
        recommendationsBinding.expLevel.text = getString(R.string.adaptive_exp_title, level)
        recommendationsBinding.expLevelNext.text = getString(R.string.adaptive_exp_subtitle, formatExp(nextLevelExp - exp))
    }

    override fun onStreak(streak: Long) {
        recommendationsBinding.expInc.text = getString(R.string.adaptive_exp_inc, streak)
        recommendationsBinding.streakSuccess.text = resources.getQuantityString(R.plurals.adaptive_streak_success, streak.toInt(), streak)

        if (streak > 1) {
            animations.playStreakSuccessAnimationSequence(
                root = recommendationsBinding.rootView,
                streakSuccessContainer = recommendationsBinding.streakSuccessContainer,
                expProgress = recommendationsBinding.expProgress,
                expInc = recommendationsBinding.expInc,
                expBubble = recommendationsBinding.expBubble
            )
        } else {
            animations.playStreakBubbleAnimation(recommendationsBinding.expInc)
        }
    }

    override fun showExpTooltip() {
        expPopupWindow = PopupHelper.showPopupAnchoredToView(requireContext(), recommendationsBinding.expBubble, getString(R.string.adaptive_exp_tooltip_text), withArrow = true)
    }

    override fun onStreakLost() {
        animations.playStreakFailedAnimation(recommendationsBinding.streakFailed, recommendationsBinding.expProgress)
    }

    override fun showNewLevelDialog(level: Long) {
        AdaptiveLevelDialogFragment
            .newInstance(level)
            .showIfNotExists(childFragmentManager, LEVEL_DIALOG_TAG)
    }

    override fun onStart() {
        super.onStart()
        recommendationsPresenter.attachView(this)
    }

    override fun onStop() {
        recommendationsPresenter.detachView(this)
        super.onStop()
    }

    override fun onReleaseComponent() {
        App.componentManager()
            .releaseAdaptiveCourseComponent(course?.id ?: 0)
    }

    override fun onDestroy() {
        recommendationsPresenter.destroy()
        super.onDestroy()
    }
}
