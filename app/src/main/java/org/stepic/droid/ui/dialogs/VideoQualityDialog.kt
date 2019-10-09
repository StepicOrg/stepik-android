package org.stepic.droid.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import android.widget.ArrayAdapter
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.preferences.UserPreferences
import ru.nobird.android.view.base.ui.extension.argument
import java.util.concurrent.ThreadPoolExecutor
import javax.inject.Inject

class VideoQualityDialog : VideoQualityDialogBase() {
    companion object {
        fun newInstance(forPlaying: Boolean) =
            VideoQualityDialog().also {
                it.forPlaying = forPlaying
            }
    }

    @Inject
    lateinit var userPreferences: UserPreferences

    @Inject
    lateinit var analytic: Analytic

    @Inject
    lateinit var threadPoolExecutor: ThreadPoolExecutor

    private var forPlaying by argument<Boolean>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        init()

        val qualityValue = if (forPlaying) {
            userPreferences.qualityVideoForPlaying
        } else {
            userPreferences.qualityVideo
        }

        val chosenOptionPosition = qualityToPositionMap[qualityValue]!!

        val adapter = ArrayAdapter<String>(requireContext(), R.layout.simple_list_item_single_choice, resources.getStringArray(R.array.video_quality))

        val builder = AlertDialog.Builder(requireContext())
        builder
            .setTitle(
                if (forPlaying) {
                    R.string.video_quality_playing
                }
                else {
                    R.string.video_quality
                }
            )
            .setSingleChoiceItems(adapter, chosenOptionPosition) { dialog, which ->
                val qualityString = positionToQualityMap[which]
                analytic.reportEventWithIdName(Analytic.Preferences.VIDEO_QUALITY, which.toString(), qualityString)

                threadPoolExecutor.execute {
                    if (forPlaying) {
                        userPreferences.saveVideoQualityForPlaying(qualityString)
                    } else {
                        userPreferences.storeQualityVideo(qualityString)
                    }
                }
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel) { _, _ ->
                analytic.reportEvent(Analytic.Interaction.CANCEL_VIDEO_QUALITY)
            }

        return builder.create()
    }

    override fun injectDependencies() {
        App.component().inject(this)
    }
}
