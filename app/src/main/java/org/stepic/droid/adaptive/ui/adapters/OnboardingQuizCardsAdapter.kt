package org.stepic.droid.adaptive.ui.adapters

import android.view.View

class OnboardingQuizCardsAdapter(private val onCardPolled: (Int) -> Unit) : QuizCardsAdapter(null, null) {

    override fun onBindViewHolder(holder: QuizCardViewHolder, pos: Int) {
        super.onBindViewHolder(holder, pos)
        holder.quizViewContainer.visibility = View.GONE
        holder.question.webView.onImageClickListener = null

        holder.separatorAnswers.visibility = View.GONE

        holder.scrollContainer.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
    }

    override fun onBindTopCard(holder: QuizCardViewHolder, pos: Int) {
        super.onBindTopCard(holder, pos)
        holder.actionButton.visibility = View.GONE

        when (getItemCount()) {
            4, 1 -> {
                holder.nextButton.visibility = View.VISIBLE
                holder.container.isEnabled = false
            }
        }
    }

    override fun poll() {
        super.poll()
        onCardPolled(getItemCount())
    }
}