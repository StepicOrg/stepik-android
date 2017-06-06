package org.stepic.droid.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.support.v7.app.AlertDialog
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.model.Video
import org.stepic.droid.model.VideoUrl
import org.stepic.droid.preferences.UserPreferences
import org.stepic.droid.util.AppConstants
import java.util.concurrent.ThreadPoolExecutor
import javax.inject.Inject

class VideoQualityDialogInPlayer : VideoQualityDialogBase() {

    interface Callback {
        fun onQualityChanged(newUrlQuality: VideoUrl)
    }

    companion object {
        private val externalVideoKey = "externalVideoKey"
        private val cachedVideoKey = "cachedVideoKey"
        private val nowPlayingKey = "nowPlaying"


        fun newInstance(externalVideo: Video, cachedVideo: Video?, nowPlayingUrl: String): VideoQualityDialogInPlayer {
            val fragment = VideoQualityDialogInPlayer()
            val bundle = Bundle()
            bundle.putParcelable(externalVideoKey, externalVideo)
            bundle.putParcelable(cachedVideoKey, cachedVideo)
            bundle.putString(nowPlayingKey, nowPlayingUrl)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun injectDependencies() {
        App.component().inject(this)
    }

    @Inject
    lateinit var userPreferences: UserPreferences

    @Inject
    lateinit var analytic: Analytic

    @Inject
    lateinit var threadPoolExecutor: ThreadPoolExecutor


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        init()

        val externalVideo = arguments.getParcelable<Video>(externalVideoKey)
        val cachedVideo = arguments.getParcelable<Video>(cachedVideoKey)
        val nowPlayingUrl = arguments.getString(nowPlayingKey)

        var position = 0
        let {
            externalVideo.urls.forEach {
                if (it.url != nowPlayingUrl) {
                    position++
                } else {
                    return@let
                }
            }
        }
        // if it is not external, than position will be after all external qualities
        val listOfUrls: MutableList<String> =
                externalVideo
                        .urls
                        .map { it.quality }
                        .filter {
                            val qualityInt = Integer.parseInt(it)
                            qualityInt <= AppConstants.MAX_QUALITY_INT
                        }
                        .toMutableList()

        cachedVideo?.urls?.firstOrNull()?.quality?.let {
            listOfUrls.add(getString(R.string.video_player_downloaded_quality, it))
        }


        val builder = AlertDialog.Builder(activity)
        builder
                .setTitle(R.string.video_quality_playing)
                .setNegativeButton(R.string.cancel) { _, _ ->
                    analytic.reportEvent(Analytic.Video.CANCEL_VIDEO_QUALITY)
                }
                .setSingleChoiceItems(listOfUrls.toTypedArray(),
                        position,
                        { dialog, which ->
                            val urlQuality =
                                    if (which in 0..externalVideo.urls.size - 1) {
                                        externalVideo.urls[which]
                                    } else {
                                        cachedVideo.urls.first()
                                    }
                            (targetFragment as Callback).onQualityChanged(newUrlQuality = urlQuality)
                            dialog.dismiss()

                            val qualityForPlaying = listOfUrls[which]
                            threadPoolExecutor.execute {
                                val toSave = findNearest(qualityForPlaying, qualityToPositionMap.keys)
                                userPreferences.saveVideoQualityForPlaying(toSave)
                            }
                        })

        return builder.create()
    }

    private fun findNearest(qualityForPlaying: String, availableQualities: Iterable<String>): String? {
        try {
            val weWant = Integer.parseInt(qualityForPlaying)
            var result: String? = null
            var bestDelta: Int = Int.MAX_VALUE
            availableQualities.forEach {
                val current = Integer.parseInt(it)
                val currentDelta = Math.abs(current - weWant)
                if (currentDelta < bestDelta) {
                    result = it
                    bestDelta = currentDelta
                }
                if (currentDelta == 0) {
                    return result
                }
            }
            return result
        } catch (exception: Exception) {
            //when it can happen?
            analytic.reportError(Analytic.Error.CANT_PARSE_QUALITY, exception)
            return null
        }

    }

}