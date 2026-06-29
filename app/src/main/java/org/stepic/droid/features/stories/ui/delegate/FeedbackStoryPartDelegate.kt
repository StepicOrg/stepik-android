package org.stepic.droid.features.stories.ui.delegate

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.view.*
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.core.widget.TextViewCompat
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import org.stepic.droid.R
import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.databinding.ViewStoryFeedbackBinding
import org.stepic.droid.features.stories.model.FeedbackStoryPart
import org.stepic.droid.ui.util.setOnKeyboardOpenListener
import org.stepik.android.model.StoryTemplate
import ru.nobird.android.stories.model.Story
import ru.nobird.android.stories.model.StoryPart
import ru.nobird.android.stories.ui.custom.DismissableLayout
import ru.nobird.android.stories.ui.custom.StoryView
import ru.nobird.android.stories.ui.delegate.StoryPartViewDelegate
import ru.nobird.android.view.base.ui.extension.hideKeyboard

class FeedbackStoryPartDelegate(
    private val analytic: Analytic,
    private val context: Context,
    private val dismissableLayout: DismissableLayout
) : StoryPartViewDelegate() {
    private companion object {
        private const val DEFAULT_TEXT_COLOR = "ffffff"
    }

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

    override fun onBindView(storyView: StoryView, container: ViewGroup, position: Int, part: StoryPart): View {
        val binding = ViewStoryFeedbackBinding.inflate(LayoutInflater.from(context), container, false)
        part as FeedbackStoryPart
        (context as? AppCompatActivity)?.currentFocus?.clearFocus()

        Glide.with(context)
            .load(part.cover)
            .placeholder(progressDrawable)
            .into(binding.storyCover)

        val story = storyView.adapter?.story
        if (story != null) {
            analytic.reportAmplitudeEvent(
                AmplitudeAnalytic.Stories.STORY_PART_OPENED, mapOf(
                AmplitudeAnalytic.Stories.Values.STORY_ID to story.id,
                AmplitudeAnalytic.Stories.Values.POSITION to position
            ))
        }

        setUpText(binding, part.text)
        setUpButton(story, binding, part.button, position)
        setUpInput(binding, storyView, part.feedback)
        return binding.root
    }

    private fun setUpText(binding: ViewStoryFeedbackBinding, text: StoryTemplate.Text?) {
        if (text != null) {
            val textColorValue = text.textColor?.takeIf(String::isNotBlank) ?: DEFAULT_TEXT_COLOR
            @ColorInt val textColor = getColorInt(textColorValue)
            binding.storyTitle.setTextColor(textColor)
            binding.storyTitle.text = text.title
            binding.storyTitle.isVisible = text.title?.isNotBlank() ?: false
        }
    }

    private fun setUpButton(story: Story?, binding: ViewStoryFeedbackBinding, button: StoryTemplate.Button?, position: Int) {
        val storyButton = binding.storyButton
        val storyFeedbackEditText = binding.storyInputContainer.storyFeedbackEditText
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

                val textToSend = storyFeedbackEditText.text?.toString()
                if (story != null && textToSend != null) {
                    analytic.reportAmplitudeEvent(AmplitudeAnalytic.Stories.STORY_FEEDBACK_PRESSED, mapOf(
                        AmplitudeAnalytic.Stories.Values.STORY_ID to story.id,
                        AmplitudeAnalytic.Stories.Values.POSITION to position,
                        AmplitudeAnalytic.Stories.Values.FEEDBACK to textToSend
                    ))
                }
            }
            storyButton.text = button.title
        }
    }

    private fun setUpInput(binding: ViewStoryFeedbackBinding, storyView: StoryView, feedback: StoryTemplate.Feedback?) {
        if (feedback == null) return
        val title = binding.storyTitle
        val storyInputBinding = binding.storyInputContainer
        val storyFeedbackText = storyInputBinding.storyFeedbackText
        val storyFeedbackIcon = storyInputBinding.storyFeedbackIcon
        val storyFeedbackEditText = storyInputBinding.storyFeedbackEditText

        storyInputBinding.root.background = getColoredDrawable(R.drawable.bg_shape_rounded, feedback.backgroundColor)
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
        storyFeedbackEditText.background = getColoredDrawable(R.drawable.bg_shape_rounded, feedback.inputBackgroundColor)
        storyFeedbackEditText.hint = feedback.placeholderText
        storyFeedbackEditText.setHintTextColor(getColorInt(feedback.placeholderTextColor))

        storyFeedbackEditText.setOnFocusChangeListener { _, hasFocus ->
            binding.root.isFocusableInTouchMode = hasFocus
            binding.root.isFocusable = hasFocus
            binding.root.isClickable = hasFocus
            storyFeedbackEditText.post {
                dismissableLayout.isFocusable = !hasFocus
                dismissableLayout.isFocusableInTouchMode = !hasFocus
                dismissableLayout.isEnabled = !hasFocus
            }

            if (!hasFocus) {
                storyFeedbackEditText.hideKeyboard()
                storyFeedbackEditText.clearFocus()
            }
        }

        setOnKeyboardOpenListener(storyView,
            {
                title.isVisible = false
            },
            {
                title.isVisible = true
                storyFeedbackEditText.clearFocus()
            }
        )
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
