package org.stepic.droid.features.stories.ui.delegate

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.widget.TextViewCompat
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.view_story_feedback.view.*
import kotlinx.android.synthetic.main.view_story_text_input.view.*
import org.stepic.droid.R
import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.features.stories.model.FeedbackStoryPart
import org.stepik.android.model.StoryTemplate
import ru.nobird.android.stories.model.Story
import ru.nobird.android.stories.model.StoryPart
import ru.nobird.android.stories.ui.custom.StoryView
import ru.nobird.android.stories.ui.delegate.StoryPartViewDelegate
import ru.nobird.android.view.base.ui.extension.hideKeyboard
import ru.nobird.android.view.base.ui.extension.inflate

class FeedbackStoryPartDelegate(
    private val analytic: Analytic,
    private val context: Context
) : StoryPartViewDelegate() {

    private val progressDrawable =
        CircularProgressDrawable(context).apply {
            alpha = 0x77
            strokeWidth = 5f
            centerRadius = 30f
            setColorSchemeColors(0xFFFFFF)
            start()
        }

    override fun isForViewType(part: StoryPart): Boolean =
        part is FeedbackStoryPart

    override fun onBindView(storyView: StoryView, container: ViewGroup, position: Int, part: StoryPart): View =
        container.inflate(R.layout.view_story_feedback, false).apply {
            part as FeedbackStoryPart

            Glide.with(context)
                .load(part.cover)
                .placeholder(progressDrawable)
                .into(this.storyCover)

            val story = storyView.adapter?.story
            if (story != null) {
                analytic.reportAmplitudeEvent(
                    AmplitudeAnalytic.Stories.STORY_PART_OPENED, mapOf(
                    AmplitudeAnalytic.Stories.Values.STORY_ID to story.id,
                    AmplitudeAnalytic.Stories.Values.POSITION to position
                ))
            }

            setUpText(this, part.text)
            setUpButton(story, this, part.button, position)
            setUpInput(this, storyView, part.feedback)
        }

    private fun setUpText(view: View, text: StoryTemplate.Text?) {
        if (text != null) {
            val storyTitle = view.storyTitle
            @ColorInt val textColor = getColorInt(text.textColor)
            storyTitle.setTextColor(textColor)
            storyTitle.text = text.title
        }
    }

    private fun setUpButton(story: Story?, view: View, button: StoryTemplate.Button?, position: Int) {
        val storyButton = view.storyButton
        val storyFeedbackEditText = view.storyFeedbackEditText
        if (button != null) {
            ViewCompat.setBackgroundTintList(storyButton, ColorStateList.valueOf(getColorInt(button.backgroundColor)))
            storyButton.setTextColor(getColorInt(button.textColor))
            storyButton.setOnClickListener {
                storyButton.isEnabled = false
                storyButton.text = button.feedbackTitle ?: ""
                storyFeedbackEditText.isEnabled = false
                TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    storyButton,
                    getColoredDrawable(R.drawable.ic_check_white, button.textColor),
                    null,
                    null,
                    null
                )
                if (story != null) {
                    // TODO APPS 3223 Enable when feature is finished
//                    analytic.reportAmplitudeEvent(AmplitudeAnalytic.Stories.STORY_FEEDBACK_PRESSED, mapOf(
//                        AmplitudeAnalytic.Stories.Values.STORY_ID to story.id,
//                        AmplitudeAnalytic.Stories.Values.POSITION to position,
//                        AmplitudeAnalytic.Stories.Values.FEEDBACK to
//                    ))
                }
            }
            storyButton.text = button.title
        }
    }

    private fun setUpInput(view: View, storyView: StoryView, feedback: StoryTemplate.Feedback?) {
        if (feedback == null) return
        val storyFeedbackContainer = view.storyInputContainer
        val storyFeedbackText = view.storyFeedbackText
        val storyFeedbackIcon = view.storyFeedbackIcon
        val storyFeedbackEditText = view.storyFeedbackEditText

        storyFeedbackContainer.background = getColoredDrawable(R.drawable.bg_story_feedback, feedback.backgroundColor)
        storyFeedbackText.text = feedback.text
        storyFeedbackText.setTextColor(getColorInt(feedback.textColor))

        val iconImageResource = when (feedback.iconStyle) {
            StoryTemplate.Feedback.IconStyle.DARK ->
                R.drawable.ic_story_feedback_light
            StoryTemplate.Feedback.IconStyle.LIGHT ->
                R.drawable.ic_story_feedback_dark
        }
        storyFeedbackIcon.setImageResource(iconImageResource)

        storyFeedbackEditText.setTextColor(getColorInt(feedback.inputTextColor))
        storyFeedbackEditText.background = getColoredDrawable(R.drawable.bg_story_feedback, feedback.inputBackgroundColor)
        storyFeedbackEditText.hint = feedback.placeholderText
        storyFeedbackEditText.setHintTextColor(getColorInt(feedback.placeholderTextColor))

        storyFeedbackEditText.setOnFocusChangeListener { _, hasFocus ->
            view.isFocusableInTouchMode = hasFocus
            view.isFocusable = hasFocus
            view.isClickable = hasFocus

            if (hasFocus) {
                storyView.pause()
            } else {
                storyFeedbackEditText.hideKeyboard()
                storyFeedbackEditText.clearFocus()
                storyView.resume()
            }
        }
    }

    private fun getColoredDrawable(@DrawableRes resId: Int, color: String): Drawable? =
        AppCompatResources
            .getDrawable(context, resId)
            ?.mutate()
            ?.let { DrawableCompat.wrap(it) }
            ?.also {
                DrawableCompat.setTint(it, getColorInt(color))
                DrawableCompat.setTintMode(it, PorterDuff.Mode.SRC_IN)
            }

    /**
     * Add symbol # as prefix and convert to @ColorRes Int
     */
    private fun getColorInt(color: String): Int =
        "#$color".toColorInt()
}