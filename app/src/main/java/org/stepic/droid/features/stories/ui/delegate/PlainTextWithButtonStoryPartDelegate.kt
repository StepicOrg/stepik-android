package org.stepic.droid.features.stories.ui.delegate

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.view_story_plain_text_with_button.view.*
import org.stepic.droid.R
import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.features.stories.model.PlainTextWithButtonStoryPart
import org.stepik.android.model.StoryTemplate
import ru.nobird.android.stories.model.Story
import ru.nobird.android.stories.model.StoryPart
import ru.nobird.android.stories.ui.custom.StoryView
import ru.nobird.android.stories.ui.delegate.StoryPartViewDelegate

class PlainTextWithButtonStoryPartDelegate(
    private val analytic: Analytic,
    private val context: Context
) : StoryPartViewDelegate() {
    companion object {
        private const val COLOR_MASK = 0xFF000000.toInt()
    }

    private val progressDrawable = CircularProgressDrawable(context).apply {
        alpha = 0x77
        strokeWidth = 5f
        centerRadius = 30f
        setColorSchemeColors(0xFFFFFF)
        start()
    }

    override fun isForViewType(part: StoryPart): Boolean =
            part is PlainTextWithButtonStoryPart

    override fun onBindView(storyView: StoryView, container: ViewGroup, position: Int, part: StoryPart): View =
            LayoutInflater.from(container.context).inflate(R.layout.view_story_plain_text_with_button, container, false).apply {
                part as PlainTextWithButtonStoryPart

                Glide.with(context)
                        .load(part.cover)
                        .placeholder(progressDrawable)
                        .into(this.storyCover)

                val story = storyView.adapter?.story
                if (story != null) {
                    analytic.reportAmplitudeEvent(AmplitudeAnalytic.Stories.STORY_PART_OPENED, mapOf(
                            AmplitudeAnalytic.Stories.Values.STORY_ID to story.id,
                            AmplitudeAnalytic.Stories.Values.POSITION to position
                    ))
                }

                setUpText(this, part.text)
                setUpButton(story, this, part.button, position)
            }

    private fun setUpText(view: View, text: StoryTemplate.Text?) {
        if (text != null) {
            val storyTitle = view.storyTitle
            val storyText = view.storyText

            @ColorInt val textColor = COLOR_MASK or text.textColor.toInt(16)

            storyTitle.setTextColor(textColor)
            storyText.setTextColor(textColor)

            storyTitle.text = text.title
            storyText.text = text.text
            storyText.isVisible = text.text.isNotBlank()
        }
    }

    private fun setUpButton(story: Story?, view: View, button: StoryTemplate.Button?, position: Int) {
        val storyButton = view.storyButton
        if (button != null) {
            ViewCompat.setBackgroundTintList(storyButton, ColorStateList.valueOf(COLOR_MASK or button.backgroundColor.toInt(16)))
            storyButton.setTextColor(COLOR_MASK or button.textColor.toInt(16))

            storyButton.text = button.title
            storyButton.setOnClickListener {
                val uri = Uri.parse(button.url)
                context.startActivity(Intent(Intent.ACTION_VIEW, uri))

                if (story != null) {
                    analytic.reportAmplitudeEvent(AmplitudeAnalytic.Stories.BUTTON_PRESSED, mapOf(
                            AmplitudeAnalytic.Stories.Values.STORY_ID to story.id,
                            AmplitudeAnalytic.Stories.Values.POSITION to position
                    ))
                }
            }

            storyButton.isVisible = true
        } else {
            storyButton.isVisible = false
        }
    }
}