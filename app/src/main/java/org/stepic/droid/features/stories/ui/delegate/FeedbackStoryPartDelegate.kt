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
import ru.nobird.android.view.base.ui.extension.inflate

class FeedbackStoryPartDelegate(
    private val analytic: Analytic,
    private val context: Context,
) : StoryPartViewDelegate() {
    companion object {
        private const val COLOR_MASK = 0xFF000000.toInt()
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
            setUpInput(this, part.feedback)
        }

    private fun setUpText(view: View, text: StoryTemplate.Text?) {
        if (text != null) {
            val storyTitle = view.storyTitle

            @ColorInt val textColor = COLOR_MASK or text.textColor.toInt(16)

            storyTitle.setTextColor(textColor)
            storyTitle.text = text.title
        }
    }

    private fun setUpButton(story: Story?, view: View, button: StoryTemplate.Button?, position: Int) {
        val storyButton = view.storyButton
        val storyFeedbackEditText = view.storyFeedbackEditText
        if (button != null) {
            ViewCompat.setBackgroundTintList(storyButton, ColorStateList.valueOf(COLOR_MASK or button.backgroundColor.toInt(16)))
            storyButton.setTextColor(COLOR_MASK or button.textColor.toInt(16))
            storyButton.setOnClickListener {
                storyButton.isEnabled = false
                storyButton.text = button.feedbackTitle ?: ""
                storyFeedbackEditText.isEnabled = false
                storyButton.setCompoundDrawables(
                    getColoredDrawable(R.drawable.ic_check_white, COLOR_MASK or button.textColor.toInt(16)),
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

    private fun setUpInput(view: View, feedback: StoryTemplate.Feedback?) {
        if (feedback == null) return
        val storyFeedbackContainer = view.storyInputContainer
        val storyFeedbackText = view.storyFeedbackText
        val storyFeedbackIcon = view.storyFeedbackIcon
        val storyFeedbackEditText = view.storyFeedbackEditText

        storyFeedbackContainer.background = getColoredDrawable(R.drawable.bg_story_feedback, "#${feedback.backgroundColor}".toColorInt())
        storyFeedbackText.text = feedback.text
        storyFeedbackText.setTextColor(COLOR_MASK or feedback.textColor.toInt(16))

        val iconImageResource = when (feedback.iconStyle) {
            StoryTemplate.Feedback.IconStyle.DARK ->
                R.drawable.ic_story_feedback_light
            StoryTemplate.Feedback.IconStyle.LIGHT ->
                R.drawable.ic_story_feedback_dark
        }
        storyFeedbackIcon.setImageResource(iconImageResource)

        storyFeedbackEditText.setTextColor(COLOR_MASK or feedback.inputTextColor.toInt(16))
        storyFeedbackEditText.background = getColoredDrawable(R.drawable.bg_story_feedback, "#${feedback.inputBackgroundColor}".toColorInt())
        storyFeedbackEditText.hint = feedback.placeholderText
        storyFeedbackEditText.setHintTextColor("#${feedback.placeholderTextColor}".toColorInt())
    }

    private fun getColoredDrawable(@DrawableRes resId: Int, @ColorInt color: Int): Drawable? =
        AppCompatResources
            .getDrawable(context, resId)
            ?.mutate()
            ?.let { DrawableCompat.wrap(it) }
            ?.also {
                DrawableCompat.setTint(it, color)
                DrawableCompat.setTintMode(it, PorterDuff.Mode.SRC_IN)
            }
}