package org.stepic.droid.features.stories.ui.delegate

import android.content.Context
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.support.annotation.ColorInt
import android.support.annotation.DrawableRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.view_story_plain_text_with_button.view.*
import org.stepic.droid.R
import org.stepic.droid.features.stories.model.PlainTextWithButtonStoryPart
import org.stepic.droid.ui.util.changeVisibility
import org.stepik.android.model.StoryTemplate
import ru.nobird.android.stories.model.StoryPart
import ru.nobird.android.stories.ui.custom.StoryView
import ru.nobird.android.stories.ui.delegate.StoryPartViewDelegate

class PlainTextWithButtonStoryPartDelegate(private val context: Context) : StoryPartViewDelegate() {
    companion object {
        private const val DARK_BACKGROUND_STYLE = "dark"
    }

    override fun isForViewType(part: StoryPart): Boolean =
            part is PlainTextWithButtonStoryPart

    override fun onBindView(storyView: StoryView, container: ViewGroup, position: Int, part: StoryPart): View =
            LayoutInflater.from(container.context).inflate(R.layout.view_story_plain_text_with_button, container, false).apply {
                part as PlainTextWithButtonStoryPart

                setUpText(this, part.text)
                setUpButton(this, part.button)
            }

    private fun setUpText(view: View, text: StoryTemplate.Text?) {
        val storyTextContainer = view.storyTextContainer
        if (text != null) {
            @DrawableRes val textBackgroundRes = if (text.backgroundStyle == DARK_BACKGROUND_STYLE) {
                R.drawable.stories_text_background_dark
            } else {
                R.drawable.stories_text_background_light
            }
            storyTextContainer.setBackgroundResource(textBackgroundRes)

            val storyTitle = view.storyTitle
            val storyText = view.storyText

            @ColorInt val textColor = text.textColor.toInt(16)

            storyTitle.setTextColor(textColor)
            storyText.setTextColor(textColor)

            storyTitle.text = text.title
            storyText.text = text.text

            storyTextContainer.changeVisibility(true)
        } else {
            storyTextContainer.changeVisibility(false)
        }
    }

    private fun setUpButton(view: View, button: StoryTemplate.Button?) {
        val storyButton = view.storyButton
        if (button != null) {
            val backgroundDrawable = GradientDrawable()
            backgroundDrawable.setColor(button.backgroundColor.toInt(16))
            backgroundDrawable.cornerRadius = view.context.resources.getDimension(R.dimen.stories_default_corner_radius)

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                @Suppress("DEPRECATION")
                storyButton.setBackgroundDrawable(backgroundDrawable)
            } else {
                storyButton.background = backgroundDrawable
            }

            storyButton.setTextColor(button.textColor.toInt(16))

            storyButton.text = button.title
            storyButton.setOnClickListener {
                val uri = Uri.parse(button.url)
                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
            }

            storyButton.changeVisibility(true)
        } else {
            storyButton.changeVisibility(false)
        }
    }
}