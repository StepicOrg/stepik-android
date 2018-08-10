package org.stepic.droid.ui.dialogs

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.widget.CheckBox
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.concurrency.MainHandler
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.preferences.UserPreferences
import org.stepic.droid.util.argument
import timber.log.Timber
import java.util.concurrent.ThreadPoolExecutor
import javax.inject.Inject

class VideoQualityDetailedDialog : VideoQualityDialogBase() {

    companion object {
        const val TAG = "VideoQualityDetailedDialog"
        const val VIDEO_QUALITY_REQUEST_CODE = 9048
        const val POSITION_KEY = "position"

        fun newInstance(position: Int): VideoQualityDetailedDialog {
            val dialog = VideoQualityDetailedDialog()
            dialog.position = position
            return dialog
        }
    }

    @Inject
    lateinit var analytic: Analytic

    @Inject
    lateinit var userPreferences: UserPreferences

    @Inject
    lateinit var threadPoolExecutor: ThreadPoolExecutor

    @Inject
    lateinit var mainHandler: MainHandler

    @Inject
    lateinit var sharedPreferencesHelper: SharedPreferenceHelper

    private var onLoadPositionListener: OnLoadPositionListener? = null

    private var position by argument<Int>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        init()
        val layoutInflater = LayoutInflater.from(context)
        val explanationView = layoutInflater.inflate(R.layout.not_ask_again_view, null)
        val checkbox = explanationView.findViewById<CheckBox>(R.id.do_not_ask_checkbox)

        var chosenOptionPosition = qualityToPositionMap[userPreferences.qualityVideo]!!

        val builder = AlertDialog.Builder(activity)
        builder
                .setTitle(R.string.video_quality)
                .setView(explanationView)
                .setNegativeButton(R.string.cancel) { _, _ ->
                    analytic.reportEvent(Analytic.Interaction.CANCEL_VIDEO_QUALITY_DETAILED)
                }
                .setSingleChoiceItems(R.array.video_quality, qualityToPositionMap[userPreferences.qualityVideo]!!) { _, which ->
                    chosenOptionPosition = which
                }
                .setPositiveButton(R.string.ok) { _, _ ->
                    val qualityString = positionToQualityMap[chosenOptionPosition]
                    analytic.reportEventWithIdName(Analytic.Preferences.VIDEO_QUALITY, chosenOptionPosition.toString(), qualityString)

                    threadPoolExecutor.execute {
                        userPreferences.storeQualityVideo(qualityString)
                        mainHandler.post {
                            targetFragment?.onActivityResult(VIDEO_QUALITY_REQUEST_CODE, Activity.RESULT_OK,
                                    Intent().putExtra(POSITION_KEY, position))
                        }
                    }

                    val isNeedExplanation = !checkbox.isChecked
                    threadPoolExecutor.execute {
                        sharedPreferencesHelper.isNeedToShowVideoQualityExplanation = isNeedExplanation
                    }
                }


        return builder.create()
    }

    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)
        Timber.d("onDismiss")
        onLoadPositionListener = null

    }

    override fun injectDependencies() {
        App.component().inject(this)
    }

    fun setOnLoadPositionListener(onLoadPositionListener: OnLoadPositionListener) {
        this.onLoadPositionListener = onLoadPositionListener
    }
}
