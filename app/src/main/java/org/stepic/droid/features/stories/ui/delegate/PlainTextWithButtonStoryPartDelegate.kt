package org.stepic.droid.features.stories.ui.delegate

import android.content.Context
import android.content.res.ColorStateList
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import org.stepic.droid.R
import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.databinding.ViewStoryPlainTextWithButtonBinding
import org.stepic.droid.features.stories.model.PlainTextWithButtonStoryPart
import org.stepik.android.domain.story.model.StoryReaction
import org.stepik.android.model.StoryTemplate
import org.stepik.android.view.base.routing.InternalDeeplinkRouter
import ru.nobird.android.stories.model.Story
import ru.nobird.android.stories.model.StoryPart
import ru.nobird.android.stories.ui.custom.StoryView
import ru.nobird.android.stories.ui.delegate.StoryPartViewDelegate

class PlainTextWithButtonStoryPartDelegate(
    private val analytic: Analytic,
    private val context: Context,
    private val storyReactions: Map<Long, StoryReaction>,
    private val storyReactionListener: (storyId: Long, storyPosition: Int, storyReaction: StoryReaction) -> Unit
) : StoryPartViewDelegate() {
    companion object {
        private const val COLOR_MASK = 0xFF000000.toInt()
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
        part is PlainTextWithButtonStoryPart

    override fun onBindView(storyView: StoryView, container: ViewGroup, position: Int, part: StoryPart): View {
        val binding = ViewStoryPlainTextWithButtonBinding.inflate(LayoutInflater.from(context), container, false)
        part as PlainTextWithButtonStoryPart
        (context as? AppCompatActivity)?.currentFocus?.clearFocus()

        Glide.with(context)
            .load(part.cover)
            .placeholder(progressDrawable)
            .into(binding.storyCover)

        val story = storyView.adapter?.story
        if (story != null) {
            analytic.reportAmplitudeEvent(AmplitudeAnalytic.Stories.STORY_PART_OPENED, mapOf(
                AmplitudeAnalytic.Stories.Values.STORY_ID to story.id,
                AmplitudeAnalytic.Stories.Values.POSITION to position
            ))
        }

        setUpText(binding, part.text)
        setUpButton(story, binding, part.button, position)
        setUpReactions(story, binding, position)
        return binding.root
    }

    private fun setUpText(binding: ViewStoryPlainTextWithButtonBinding, text: StoryTemplate.Text?) {
        if (text != null) {
            val textColorValue = text.textColor?.takeIf(String::isNotBlank) ?: DEFAULT_TEXT_COLOR
            @ColorInt val textColor = COLOR_MASK or textColorValue.toInt(16)

            binding.storyTitle.setTextColor(textColor)
            binding.storyText.setTextColor(textColor)

            binding.storyTitle.text = text.title
            binding.storyTitle.isVisible = text.title?.isNotBlank() ?: false
            binding.storyText.text = text.text
            binding.storyText.isVisible = text.text?.isNotBlank() ?: false
        }
    }

    private fun setUpButton(story: Story?, binding: ViewStoryPlainTextWithButtonBinding, button: StoryTemplate.Button?, position: Int) {
        val storyButton = binding.storyButton
        if (button != null) {
            ViewCompat.setBackgroundTintList(storyButton, ColorStateList.valueOf(COLOR_MASK or button.backgroundColor.toInt(16)))
            storyButton.setTextColor(COLOR_MASK or button.textColor.toInt(16))

            storyButton.text = button.title
            storyButton.setOnClickListener {
                val uri = Uri.parse(button.url)
                InternalDeeplinkRouter.openInternalDeeplink(context, uri)

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

    fun setUpReactions(story: Story?, binding: ViewStoryPlainTextWithButtonBinding, position: Int) {
        val storyId = story?.id ?: 0
        val vote = storyReactions[storyId]

        with(binding.storyReactionLike) {
            setOnClickListener {
                val id = story?.id ?: return@setOnClickListener
                storyReactionListener.invoke(id, position, StoryReaction.LIKE)
            }
            isActivated = vote == StoryReaction.LIKE
        }
        with(binding.storyReactionDislike) {
            setOnClickListener {
                val id = story?.id ?: return@setOnClickListener
                storyReactionListener.invoke(id, position, StoryReaction.DISLIKE)
            }
            isActivated = vote == StoryReaction.DISLIKE
        }
    }
}
