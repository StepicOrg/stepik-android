package org.stepic.droid.adaptive.ui.adapters

import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.Button
import androidx.annotation.StringRes
import androidx.core.widget.doOnTextChanged
import org.stepic.droid.R
import org.stepic.droid.adaptive.ui.animations.CardAnimations
import org.stepic.droid.adaptive.ui.custom.CardScrollView
import org.stepic.droid.adaptive.ui.custom.SwipeableLayout
import org.stepic.droid.adaptive.ui.custom.container.ContainerView
import org.stepic.droid.base.App
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.core.presenters.CardPresenter
import org.stepic.droid.core.presenters.contracts.CardView
import org.stepic.droid.databinding.AdaptiveQuizCardViewBinding
import org.stepic.droid.ui.quiz.QuizDelegate
import org.stepic.droid.ui.util.snackbar
import org.stepic.droid.util.resolvers.StepTypeResolver
import org.stepik.android.model.Step
import org.stepik.android.model.Submission
import org.stepik.android.model.adaptive.Reaction
import org.stepik.android.view.base.ui.extension.ExternalLinkWebViewClient
import org.stepik.android.view.latex.ui.widget.LatexView
import javax.inject.Inject
import kotlin.math.max

class QuizCardViewHolder(
    private val root: View
) : ContainerView.ViewHolder(root), CardView {
    private val binding = AdaptiveQuizCardViewBinding.bind(root)

    private val curtain = binding.curtain
    private val answersProgress = binding.answersProgress
    private val titleView = binding.title
    val question: LatexView = binding.question
    val quizViewContainer: ViewGroup = binding.quizViewContainer
    val separatorAnswers: View = binding.separatorAnswers.root

    val actionButton: Button = binding.submit
    val nextButton: Button = binding.next
    private val correctSign = binding.correct
    private val wrongSign = binding.wrong
    private val wrongButton = binding.wrongRetry
    private val hint = binding.hint

    val scrollContainer: CardScrollView = binding.scroll
    val container: SwipeableLayout = binding.container

    private val hardReaction = binding.reactionHard
    private val easyReaction = binding.reactionEasy

    val cardView: androidx.cardview.widget.CardView = binding.card

    private lateinit var quizDelegate: QuizDelegate

    @Inject
    lateinit var screenManager: ScreenManager

    @Inject
    lateinit var stepTypeResolver: StepTypeResolver

    init {
        App.component().inject(this)

        question.webViewClient = object : ExternalLinkWebViewClient(root.context) {
            override fun onPageFinished(view: WebView?, url: String?) {
                onCardLoaded()
            }
        }
        question.textView.doOnTextChanged { _, _, _, _ ->
            onCardLoaded()
        }
        question.webView.setLayerType(View.LAYER_TYPE_NONE, null)

        nextButton.setOnClickListener { container.swipeDown() }
        actionButton.setOnClickListener { presenter?.createSubmission() }
        wrongButton.setOnClickListener { _ ->
            presenter?.let {
                it.retrySubmission()
                quizDelegate.isEnabled = true
                resetSupplementalActions()
            }
        }
        container.setNestedScroll(scrollContainer)
    }

    private var hasSubmission = false
    private var presenter: CardPresenter? = null

    fun bind(presenter: CardPresenter) {
        this.presenter = presenter
        presenter.attachView(this)
    }

    fun onTopCard() {
        if (!hasSubmission) {
            if (presenter?.isLoading == true) {
                onSubmissionLoading()
            } else {
                actionButton.visibility = View.VISIBLE
                quizDelegate.isEnabled = true
            }
        }

        container.setSwipeListener(object : SwipeableLayout.SwipeListener() {
            override fun onScroll(scrollProgress: Float) {
                hardReaction.alpha = max(2 * scrollProgress, 0f)
                easyReaction.alpha = max(2 * -scrollProgress, 0f)
            }

            override fun onSwipeLeft() {
                easyReaction.alpha = 1f
                presenter?.createReaction(Reaction.NEVER_AGAIN)
            }

            override fun onSwipeRight() {
                hardReaction.alpha = 1f
                presenter?.createReaction(Reaction.MAYBE_LATER)
            }
        })
    }

    private fun onCardLoaded() {
        curtain.visibility = View.GONE
        if (presenter?.isLoading != true) answersProgress.visibility = View.GONE
    }

    override fun setStep(step: Step?) {
        quizViewContainer.removeAllViews()
        quizDelegate = stepTypeResolver.getQuizDelegate(step)

        quizViewContainer.addView(quizDelegate.createView(quizViewContainer))
        quizDelegate.actionButton = actionButton
    }

    override fun setTitle(title: String?) {
        title?.let { titleView.text = it }
    }

    override fun setQuestion(html: String?) {
        html?.let { question.setText(it) }
    }

    override fun setSubmission(submission: Submission, animate: Boolean) {
        resetSupplementalActions()
        quizDelegate.setSubmission(submission)
        when (submission.status) {
            Submission.Status.CORRECT -> {
                quizDelegate.isEnabled = false
                actionButton.visibility = View.GONE
                hasSubmission = true

                correctSign.visibility = View.VISIBLE
                nextButton.visibility = View.VISIBLE
                container.isEnabled = true

                if (submission.hint?.isNotBlank() == true) {
                    hint.text = submission.hint
                    hint.visibility = View.VISIBLE
                }

                if (animate) {
                    scrollDown()
                }
            }

            Submission.Status.WRONG -> {
                quizDelegate.isEnabled = false
                wrongSign.visibility = View.VISIBLE
                hasSubmission = true

                wrongButton.visibility = View.VISIBLE
                actionButton.visibility = View.GONE

                container.isEnabled = true

                if (animate) {
                    CardAnimations.playWiggleAnimation(container)
                }
            }

            else -> Unit
        }
    }

    override fun onSubmissionConnectivityError() = onSubmissionError(R.string.no_connection)

    override fun onSubmissionRequestError() = onSubmissionError(R.string.request_error)

    private fun onSubmissionError(@StringRes errorMessage: Int) {
        (root.parent as? ViewGroup)
            ?.snackbar(messageRes = errorMessage)

        container.isEnabled = true
        quizDelegate.isEnabled = true
        resetSupplementalActions()
    }

    override fun onSubmissionLoading() {
        resetSupplementalActions()
        container.isEnabled = false
        quizDelegate.isEnabled = false
        actionButton.visibility = View.GONE
        answersProgress.visibility = View.VISIBLE

        scrollDown()
    }

    override fun getQuizViewDelegate() = quizDelegate

    private fun scrollDown() {
        scrollContainer.post {
            scrollContainer.fullScroll(View.FOCUS_DOWN)
        }
    }

    private fun resetSupplementalActions() {
        nextButton.visibility = View.GONE
        correctSign.visibility = View.GONE
        wrongSign.visibility = View.GONE
        wrongButton.visibility = View.GONE
        answersProgress.visibility = View.GONE
        hint.visibility = View.GONE
        actionButton.visibility = View.VISIBLE
    }
}
