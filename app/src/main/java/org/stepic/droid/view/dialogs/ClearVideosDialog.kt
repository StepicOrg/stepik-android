package org.stepic.droid.view.dialogs

import android.app.Dialog
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import com.squareup.otto.Bus
import com.yandex.metrica.YandexMetrica
import org.stepic.droid.R
import org.stepic.droid.base.MainApplication
import org.stepic.droid.concurrency.IMainHandler
import org.stepic.droid.events.loading.FinishDeletingLoadEvent
import org.stepic.droid.events.loading.StartDeletingLoadEvent
import org.stepic.droid.events.steps.ClearAllDownloadWithoutAnimationEvent
import org.stepic.droid.preferences.UserPreferences
import org.stepic.droid.store.CleanManager
import org.stepic.droid.store.operations.DatabaseFacade
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.DbParseHelper
import org.stepic.droid.util.FileUtil
import java.util.concurrent.ThreadPoolExecutor
import javax.inject.Inject

class ClearVideosDialog : DialogFragment() {
    @Inject
    lateinit var mDatabaseFacade: DatabaseFacade
    @Inject
    lateinit var mCleanManager: CleanManager
    @Inject
    lateinit var mBus: Bus
    @Inject
    lateinit var threadPoolExecutor: ThreadPoolExecutor
    @Inject
    lateinit var mainHandler: IMainHandler

    @Inject
    lateinit var userPreferences: UserPreferences

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        MainApplication.component().inject(this)
        val bundle = arguments
        val stringIds = bundle?.getString(KEY_STRING_IDS)


        val builder = AlertDialog.Builder(activity, R.style.MyAlertDialogStyle)
        builder.setTitle(R.string.title_confirmation).setMessage(R.string.clear_videos).setPositiveButton(R.string.yes) { dialog, which ->
            YandexMetrica.reportEvent(AppConstants.METRICA_YES_CLEAR_VIDEOS)

            val task = object : AsyncTask<Void, Void, Void>() {
                override fun onPreExecute() {
                    super.onPreExecute()
                    mBus.post(StartDeletingLoadEvent())

                }

                override fun doInBackground(params: Array<Void>): Void? {
                    val stepIds: LongArray?
                    if (stringIds != null) {

                        stepIds = DbParseHelper.parseStringToLongArray(stringIds)
                        if (stepIds == null) return null
                        for (stepId in stepIds) {
                            val step = mDatabaseFacade.getStepById(stepId)
                            mCleanManager.removeStep(step)
                        }
                    } else {
                        stepIds = null
                        FileUtil.cleanDirectory(userPreferences.userDownloadFolder);
                        mDatabaseFacade.dropDatabase();
                    }

                    mainHandler.post {
                        mBus.post(ClearAllDownloadWithoutAnimationEvent(stepIds))
                    }
                    return null
                }

                override fun onPostExecute(o: Void?) {
                    mBus.post(FinishDeletingLoadEvent())
                }
            }
            task.executeOnExecutor(threadPoolExecutor)
        }.setNegativeButton(R.string.no, null)

        return builder.create()
    }

    companion object {
        val KEY_STRING_IDS = "step_ids"
    }
}
