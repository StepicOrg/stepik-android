package org.stepic.droid.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.MainApplication
import org.stepic.droid.preferences.UserPreferences
import java.util.*
import java.util.concurrent.ThreadPoolExecutor
import javax.inject.Inject

class VideoQualityDialog : DialogFragment() {

    @Inject
    lateinit var userPreferences: UserPreferences

    @Inject
    lateinit var analytic: Analytic

    @Inject
    lateinit var threadPoolExecutor: ThreadPoolExecutor

    private val qualityToPositionMap: MutableMap<String, Int> = HashMap()
    private val positionToQualityMap: MutableMap<Int, String> = HashMap()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        MainApplication.component().inject(this)
        if (qualityToPositionMap.isEmpty() || positionToQualityMap.isEmpty()) {
            initMaps()
        }

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

    private fun initMaps() {
        qualityToPositionMap.put("270", 0)
        qualityToPositionMap.put("360", 1)
        qualityToPositionMap.put("720", 2)
        qualityToPositionMap.put("1080", 3)

        positionToQualityMap.put(0, "270")
        positionToQualityMap.put(1, "360")
        positionToQualityMap.put(2, "720")
        positionToQualityMap.put(3, "1080")
    }
}
