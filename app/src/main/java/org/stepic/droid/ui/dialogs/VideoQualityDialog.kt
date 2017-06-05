package org.stepic.droid.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.support.v7.app.AlertDialog
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.preferences.UserPreferences
import java.util.concurrent.ThreadPoolExecutor
import javax.inject.Inject

class VideoQualityDialog : VideoQualityDialogBase() {

    companion object {
        private val forPlayingFlagKey = "forPlayingFlagKey"

        fun newInstance(forPlaying: Boolean): VideoQualityDialog {
            val bundle = Bundle()
            bundle.putBoolean(forPlayingFlagKey, forPlaying)
            val fragment = VideoQualityDialog()
            fragment.arguments = bundle
            return fragment
        }
    }

    @Inject
    lateinit var userPreferences: UserPreferences

    @Inject
    lateinit var analytic: Analytic

    @Inject
    lateinit var threadPoolExecutor: ThreadPoolExecutor

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        init()

        val forPlaying = arguments.getBoolean(forPlayingFlagKey)
        val qualityTitle = if (forPlaying) {
            userPreferences.qualityVideoForPlaying
        } else {
            userPreferences.qualityVideo
        }

        val builder = AlertDialog.Builder(activity)
        builder
                .setTitle(R.string.video_quality)
                .setNegativeButton(R.string.cancel) { _, _ ->
                    analytic.reportEvent(Analytic.Interaction.CANCEL_VIDEO_QUALITY)
                }
                .setSingleChoiceItems(R.array.video_quality,
                        qualityToPositionMap[qualityTitle]!!,
                        { dialog, which ->
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
                        })

        return builder.create()
    }

    override fun injectDependencies() {
        App.component().inject(this)
    }
}
