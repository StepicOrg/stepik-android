package org.stepic.droid.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.widget.CheckBox
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.MainApplication
import org.stepic.droid.concurrency.IMainHandler
import org.stepic.droid.preferences.UserPreferences
import timber.log.Timber
import java.util.concurrent.ThreadPoolExecutor
import javax.inject.Inject

class VideoQualityDetailedDialog : VideoQualityDialogBase() {

    companion object {
        fun newInstance(): DialogFragment {
            return VideoQualityDetailedDialog()
        }
    }

    @Inject
    lateinit var analytic: Analytic

    @Inject
    lateinit var userPreferences: UserPreferences

    @Inject
    lateinit var threadPoolExecutor: ThreadPoolExecutor

    @Inject
    lateinit var mainHandler: IMainHandler


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        init()
        val layoutInflater = LayoutInflater.from(context)
        val explanationView = layoutInflater.inflate(R.layout.quality_dialog_explanation, null)
        val checkbox = explanationView.findViewById(R.id.do_not_ask_checkbox) as CheckBox

        var chosenOptionPosition = qualityToPositionMap[userPreferences.qualityVideo]!!

        val builder = AlertDialog.Builder(activity)
        builder
                .setTitle(R.string.video_quality)
                .setView(explanationView)
                .setNegativeButton(R.string.cancel) {
                    dialog, which ->
                    analytic.reportEvent(Analytic.Interaction.CANCEL_VIDEO_QUALITY_DETAILED)
                }
                .setSingleChoiceItems(R.array.video_quality,
                        qualityToPositionMap[userPreferences.qualityVideo]!!,
                        { dialog, which -> chosenOptionPosition = which })
                .setPositiveButton(R.string.ok, { dialog, which ->
                    Timber.d(chosenOptionPosition.toString())
                    Timber.d(checkbox.isChecked.toString())
                })


        return builder.create()
    }

    override fun injectDependencies() {
        MainApplication.component().inject(this)
    }
}
