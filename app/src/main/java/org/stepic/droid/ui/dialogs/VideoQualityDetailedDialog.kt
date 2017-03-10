package org.stepic.droid.ui.dialogs

import android.app.Dialog
import android.content.DialogInterface
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
import timber.log.Timber
import java.util.concurrent.ThreadPoolExecutor
import javax.inject.Inject

class VideoQualityDetailedDialog : VideoQualityDialogBase() {

    companion object {
        val adapterPositionKey = "adapterPosKey"

        fun newInstance(adapterPosition: Int): VideoQualityDetailedDialog {
            val dialog = VideoQualityDetailedDialog()
            val bundle = Bundle()
            bundle.putInt(adapterPositionKey, adapterPosition)
            dialog.arguments = bundle
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

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        init()
        val adapterPosition = arguments.getInt(adapterPositionKey)
        val layoutInflater = LayoutInflater.from(context)
        val explanationView = layoutInflater.inflate(R.layout.not_ask_again_view, null)
        val checkbox = explanationView.findViewById(R.id.do_not_ask_checkbox) as CheckBox

        var chosenOptionPosition = qualityToPositionMap[userPreferences.qualityVideo]!!

        val builder = AlertDialog.Builder(activity)
        builder
                .setTitle(R.string.video_quality)
                .setView(explanationView)
                .setNegativeButton(R.string.cancel) { _, _ ->
                    analytic.reportEvent(Analytic.Interaction.CANCEL_VIDEO_QUALITY_DETAILED)
                }
                .setSingleChoiceItems(R.array.video_quality,
                        qualityToPositionMap[userPreferences.qualityVideo]!!,
                        { _, which -> chosenOptionPosition = which })
                .setPositiveButton(R.string.ok, { _, which ->
                    val qualityString = positionToQualityMap[chosenOptionPosition]
                    analytic.reportEventWithIdName(Analytic.Preferences.VIDEO_QUALITY, which.toString(), qualityString)

                    threadPoolExecutor.execute {
                        userPreferences.storeQualityVideo(qualityString)
                        mainHandler.post {
                            onLoadPositionListener?.onNeedLoadPosition(adapterPosition)
                        }
                    }

                    val isNeedExplanation = !checkbox.isChecked
                    threadPoolExecutor.execute {
                        sharedPreferencesHelper.isNeedToShowVideoQualityExplanation = isNeedExplanation
                    }
                })


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
