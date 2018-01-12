package org.stepic.droid.ui.adapters

import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.ImageView
import kotlinx.android.synthetic.main.stepic_compound_button.view.*
import org.stepic.droid.R
import org.stepic.droid.adaptive.ui.custom.container.ContainerView
import org.stepic.droid.model.Attempt
import org.stepic.droid.model.Reply
import org.stepic.droid.ui.custom.LatexSupportableEnhancedFrameLayout

class AttemptAnswersAdapter {
    private var options: List<String>? = null
    private var selection: BooleanArray? = null

    private var lastSelection = -1
    private var selectedCount = 0

    var enabled = true

    var attempt: Attempt? = null
        set(value) {
            field = value
            options = value?.dataset?.options
            selection = options?.let { BooleanArray(it.size) }
            lastSelection = -1
            selectedCount = 0
//            onDataSetChanged()
        }

    private var submitButton: Button? = null

    fun setSubmitButton(submitButton: Button) {
        this.submitButton = submitButton
        refreshSubmitButton()
    }

    private fun refreshSubmitButton() {
        submitButton?.isEnabled = selectedCount > 0
    }

    private fun select(pos: Int) {
        if (!enabled) return

        selection?.let {
            if (attempt?.dataset?.is_multiple_choice == true) {
                selectedCount += if (it[pos]) -1 else 1
                it[pos] = !it[pos]
            } else {
                if (lastSelection != -1) {
                    it[lastSelection] = false
//                    onRebind(lastSelection)
                    selectedCount--
                }
                it[pos] = true
                selectedCount++
            }

            lastSelection = pos
//            onRebind(pos)
            refreshSubmitButton()
        }
    }

    public fun getReply() = Reply.Builder().setChoices(selection?.toList()).build()

    fun onCreateViewHolder(parent: ViewGroup) =
            AttemptAnswerViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.stepic_compound_button, parent, false))

    fun getItemCount() = options?.size ?: 0

    fun onBindViewHolder(holder: AttemptAnswerViewHolder, pos: Int) {
        options?.let {
            holder.answerText.setText(it[pos])
            holder.view.setOnClickListener {
                select(pos)
                Log.d(javaClass.canonicalName, "onClick $pos")
            }
        }
    }


    public fun clear() {
        attempt = null
    }

    class AttemptAnswerViewHolder(rootView: View) : ContainerView.ViewHolder(rootView) {
        val answerImage: ImageView = rootView.image_compound_button
        val answerText: LatexSupportableEnhancedFrameLayout = rootView.text_compound_button

        private val answerProgress = rootView.loadProgressbar

        init {
            answerText.webView.webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    answerProgress.visibility = View.VISIBLE
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    answerProgress.visibility = View.GONE
                }
            }
        }
    }
}