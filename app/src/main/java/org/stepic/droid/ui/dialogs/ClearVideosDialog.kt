package org.stepic.droid.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.concurrency.MainHandler
import org.stepic.droid.preferences.UserPreferences
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepic.droid.util.DbParseHelper
import org.stepic.droid.util.FileUtil
import java.util.concurrent.ThreadPoolExecutor
import javax.inject.Inject

class ClearVideosDialog : DialogFragment() {

    companion object {
        val KEY_STRING_IDS = "step_ids"

        fun newInstance(): ClearVideosDialog {
            return ClearVideosDialog()
        }
    }

    @Inject
    lateinit var databaseFacade: DatabaseFacade

    @Inject
    lateinit var threadPoolExecutor: ThreadPoolExecutor

    @Inject
    lateinit var mainHandler: MainHandler

    @Inject
    lateinit var userPreferences: UserPreferences

    @Inject
    lateinit var analytic: Analytic

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        App.component().inject(this)
        val bundle = arguments
        val stringIds = bundle?.getString(KEY_STRING_IDS)


        val builder = AlertDialog.Builder(activity)
        builder.setTitle(R.string.title_confirmation).setMessage(R.string.clear_videos).setPositiveButton(R.string.yes) { _, _ ->
            analytic.reportEvent(Analytic.Interaction.YES_CLEAR_VIDEOS)

            (targetFragment as? Callback)?.onStartLoading()
            threadPoolExecutor.execute {
                try {
                    val stepIds: LongArray?
                    if (stringIds != null) {
                        stepIds = DbParseHelper.parseStringToLongArray(stringIds)
                        stepIds
                                ?.map { databaseFacade.getStepById(it) }
                                ?.forEach { cleanManager.removeStep(it) }
                    } else {
                        stepIds = null
                        FileUtil.cleanDirectory(userPreferences.userDownloadFolder);
                        FileUtil.cleanDirectory(userPreferences.sdCardDownloadFolder)
                        databaseFacade.dropDatabase();
                    }
                    mainHandler.post {
                        (targetFragment as? Callback)?.onClearAllWithoutAnimation(stepIds)
                    }
                } finally {
                    mainHandler.post { (targetFragment as? Callback)?.onFinishLoading() }
                }
            }
        }.setNegativeButton(R.string.no, null)

        return builder.create()
    }

    /**
     * set as target fragment, which implements this Callback for getting notifications
     */
    interface Callback {

        fun onStartLoading()

        fun onFinishLoading()

        fun onClearAllWithoutAnimation(stepIds: LongArray?)
    }

}
