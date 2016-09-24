package org.stepic.droid.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.support.v7.app.AlertDialog
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.MainApplication
import org.stepic.droid.preferences.UserPreferences
import java.util.concurrent.ThreadPoolExecutor
import javax.inject.Inject

class VideoQualityDialog : VideoQualityDialogBase() {
    @Inject
    lateinit var userPreferences: UserPreferences

    @Inject
    lateinit var analytic: Analytic

    @Inject
    lateinit var threadPoolExecutor: ThreadPoolExecutor

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        init()

        val builder = AlertDialog.Builder(activity)
        builder
                .setTitle(R.string.video_quality)
                .setNegativeButton(R.string.cancel) {
                    dialog, which ->
                    analytic.reportEvent(Analytic.Interaction.CANCEL_VIDEO_QUALITY)
                }
                .setSingleChoiceItems(R.array.video_quality,
                        qualityToPositionMap[userPreferences.qualityVideo]!!,
                        { dialog, which ->
                            val qualityString = positionToQualityMap[which]
                            analytic.reportEventWithIdName(Analytic.Preferences.VIDEO_QUALITY, which.toString(), qualityString)

                            threadPoolExecutor.execute {
                                userPreferences.storeQualityVideo(qualityString)
                            }
                            dialog.dismiss()
                        })

        return builder.create()
    }

    override fun injectDependencies() {
        MainApplication.component().inject(this)
    }
}
