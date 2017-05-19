package org.stepic.droid.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import com.squareup.otto.Bus
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.concurrency.MainHandler
import org.stepic.droid.core.video_moves.contract.VideosMovedPoster
import org.stepic.droid.preferences.UserPreferences
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.StorageUtil
import java.io.File
import java.util.concurrent.ThreadPoolExecutor
import javax.inject.Inject

class WantMoveDataDialog : DialogFragment() {

    @Inject
    lateinit var databaseFacade: DatabaseFacade

    @Inject
    lateinit var userPreferences: UserPreferences

    @Inject
    lateinit var threadPoolExecutor: ThreadPoolExecutor

    @Inject
    lateinit var mainHandler: MainHandler

    @Inject
    lateinit var analytic: Analytic

    @Inject
    lateinit var bus: Bus

    @Inject
    lateinit var videosMovedPoster: VideosMovedPoster

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        App.component().inject(this)

        val builder = AlertDialog.Builder(activity)
        builder.setTitle(R.string.title_confirmation)
                .setMessage(R.string.move_data_explanation)
                .setPositiveButton(R.string.yes) { _, _ ->
                    analytic.reportEvent(Analytic.Interaction.TRANSFER_DATA_YES)
                    (targetFragment as Callback).onStartLoading(isMove = true)

                    threadPoolExecutor.execute {
                        try {
                            val cachedVideos = databaseFacade.getAllCachedVideos()

                            val outputFile: File? =
                                    if (userPreferences.isSdChosen) {
                                        //sd WAS Chosen
                                        userPreferences.userDownloadFolder
                                    } else {
                                        userPreferences.sdCardDownloadFolder
                                    }
                            if (outputFile == null) {
                                mainHandler.post {
                                    (targetFragment as Callback).onFailToMove()
                                }
                                return@execute
                            }
                            val outputPath = outputFile.path // FIXME: 09.06.16 if sd is not available -> post event with fail

                            try {
                                for (video in cachedVideos) {
                                    if (video != null && video.url != null && video.stepId >= 0) {
                                        val inputPath = File(video.url).parent
                                        if (inputPath != outputPath) {
                                            StorageUtil.moveFile(inputPath, video.videoId.toString() + "", outputPath)
                                            StorageUtil.moveFile(inputPath, video.videoId.toString() + AppConstants.THUMBNAIL_POSTFIX_EXTENSION, outputPath)
                                            val newPathVideo = File(outputPath, video.videoId.toString() + "")
                                            val newPathThumbnail = File(outputPath, video.videoId.toString() + AppConstants.THUMBNAIL_POSTFIX_EXTENSION)
                                            val urlVideo = newPathVideo.path
                                            val urlThumbnail = newPathThumbnail.path
                                            video.url = urlVideo
                                            video.thumbnail = urlThumbnail
                                            databaseFacade.addVideo(video)
                                        }
                                    }
                                }

                                userPreferences.isSdChosen = !userPreferences.isSdChosen
                            } catch (ex: Exception) {
                                analytic.reportError(Analytic.Error.FAIL_TO_MOVE, ex)
                                mainHandler.post {
                                    (targetFragment as Callback).onFailToMove()
                                }
                            }
                        } finally {
                            val callback = targetFragment as Callback
                            mainHandler.post {
                                callback.onFinishLoading()
                                videosMovedPoster.videosMoved()
                            }
                        }
                    }
                }
                .setNegativeButton(R.string.no, null)

        return builder.create()
    }

    companion object {
        fun newInstance(): DialogFragment {
            return WantMoveDataDialog()
        }
    }

    interface Callback {
        fun onStartLoading(isMove: Boolean)
        fun onFinishLoading()
        fun onFailToMove()
    }
}
